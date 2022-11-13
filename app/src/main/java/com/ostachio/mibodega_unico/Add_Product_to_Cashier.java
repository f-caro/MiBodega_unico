package com.ostachio.mibodega_unico;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.ContentValues;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.CursorAdapter;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Add_Product_to_Cashier extends Activity {

    private TextView totalTxt;

    private ListView cashierListView;

    private SQLiteDatabase db;

    private String TABLENAME,           BARCODEstring,  QUANTITYstring,     oldQUANTITYstring, TOTALpriceStr, TOTALquantityStr;
    private String DESCRIPTIONstring,   PRICEstring,    PROMOTIONstring,    CATEGORYstring;
    private int screenWidth, screenHeight;

    private PopupWindow popupCashierWindowOne, popupCashierWindowTwo;

            private EditText popupCashierNumber;

    private PopupWindow popupCashierCustomItemWindow;

            private EditText    popupCashierCustomItemQuantity,
                                popupCashierCustomItemPriceUnit,
                                popupCashierCustomItemDescription,
                                popupCashierCustomItemTotal,
                                popupCashierCustomItemBarcode;

    private PopupWindow popupCashierPaymentWindow;

            private TextView    popupCashierPaymentTotal,
                                popupCashierPaymentCustomerReceives;
            private EditText    popupCashierPaymentCustomerPays;
            private Button      popupCashierPaymentUpdateButton;

    private PopupWindow popupProductWindow;
            private EditText    popupProductBarcode, popupProductDescription, popupProductPrice;
            private CheckBox    popupProductPromotionCheckbox;
            private Button      popupProductEraseRecordButton;
            private TextView    popupProductBarcodeTextTitle;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add__product_to__cashier);
    Intent fromMainScreen = getIntent();


        totalTxt    = (TextView) findViewById(R.id.cashier_total);
    cashierListView = (ListView) findViewById(R.id.cashier_list_view);
        TABLENAME   = fromMainScreen.getStringExtra("tablename");

    getCashierDatatableToList(TABLENAME);

    cashierListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override

                public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id){

                    Cursor cur = (Cursor) cashierListView.getItemAtPosition(position);
                    cur.moveToPosition(position);

                    BARCODEstring       = cur.getString(1);
                    DESCRIPTIONstring   = cur.getString(2);
                    QUANTITYstring      = cur.getString(3);
                    PRICEstring         = cur.getString(4);

                    //Toast toast = Toast.makeText(getApplicationContext(), BARCODEstring + " ---  " + DESCRIPTIONstr + " --- "
                    //        + QUANTITYstring + "---" + PRICEstring , Toast.LENGTH_SHORT);
                    // toast.show();

                    initiatePopupCashierWindowOne();
                    //return true;
                }

            }        );

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    screenHeight    = metrics.heightPixels - 80;
    screenWidth     = metrics.widthPixels;

}

                public void startScan(View v){
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                }

                public void onActivityResult(int requestCode, int resultCode, Intent intent){
                    IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

                    if (scanningResult != null){
                        BARCODEstring = scanningResult.getContents();

                        if( "".equals(BARCODEstring) | BARCODEstring == null ) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "No Scan Data Received!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else{
                            int barcodeSize = BARCODEstring.length();
                            if ( ( barcodeSize != 13 ) & (barcodeSize != 8) ) {
                                Toast toastRepeatScan = Toast.makeText(getApplicationContext(),
                                        "Error durante Scan, Intenta denuevo", Toast.LENGTH_SHORT);
                                toastRepeatScan.show();

                                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                                scanIntegrator.initiateScan();
                            } else {
                                chkCashierBarcodeToDatatable(TABLENAME, BARCODEstring);
                            }
                        }
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No Scan Data Received!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

    public void getCashierDatatableToList (String tableName){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // connecting to a CursorAdapter
            Cursor cursorListProducts = db.query(tableName,
                    new String[] {"_id", "BARCODE" , "DESCRIPTION" , "QUANTITY" , "PRICE", "TIME_STAMPING" },
                    null, null, null, null, "TIME_STAMPING DESC");

            // connecting SimpleCursorAdapter with custom layout
            CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_list_cashier_layout ,
                    cursorListProducts , new String[] {"BARCODE", "DESCRIPTION", "QUANTITY", "PRICE" },
                    new int[] {R.id.cashier_list_text_1, R.id.cashier_list_text_2, R.id.cashier_list_text_3, R.id.cashier_list_text_4} , 0);

            // connecting custom Adapter with Cashier ListView
            cashierListView.setAdapter(cursorAdapterProducts);

            Cursor cursorSumProducts = db.query(tableName, new String[] {"sum(PRICE)" , "sum(QUANTITY)"} , null, null, null, null, null );

            if(cursorSumProducts.moveToFirst()) {
                TOTALpriceStr = cursorSumProducts.getString(0);
                TOTALquantityStr = cursorSumProducts.getString(1);
                if("".equals(TOTALpriceStr)  | TOTALpriceStr == null )    {             totalTxt.setText("Total $ 0");                }
                                    else                                            {             totalTxt.setText("Total $ " + TOTALpriceStr);         }

                if("".equals(TOTALquantityStr) )   {            TOTALquantityStr = "0"; }
            }

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " No se puede leer del Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void chkCashierBarcodeToDatatable(String tableName, String scanContent){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // first check the barcode with old table entry  // // get entry from CASHIER table

            Cursor cursorBarcodeChk = db.query( tableName, new String[] {"BARCODE", "DESCRIPTION" , "QUANTITY", "PRICE"},  "BARCODE = ?",
                    new String[] {scanContent},  null, null, null );

            if(cursorBarcodeChk.moveToFirst()){
                    DESCRIPTIONstring = cursorBarcodeChk.getString(1);
                    QUANTITYstring = cursorBarcodeChk.getString(2);
                    PRICEstring = cursorBarcodeChk.getString(3);
                    getCashierDatatableToList(tableName);
                    initiatePopupCashierWindowOne();

               // Toast toast = Toast.makeText(this," Value of QUANTITYstring = " + QUANTITYstring , Toast.LENGTH_LONG);
               // toast.show();
            }
            else {

                Cursor cursorProductBarcodeChk = db.query( "PRODUCTS", new String[] {"BARCODE", "DESCRIPTION", "QUANTITY" , "PRICE" }
                        ,  "BARCODE = ?", new String[] {BARCODEstring},  null, null, null );

                if(cursorProductBarcodeChk.moveToFirst()) {
                    DESCRIPTIONstring   = cursorProductBarcodeChk.getString(1);
                    QUANTITYstring      = cursorProductBarcodeChk.getString(2);
                    PRICEstring         = cursorProductBarcodeChk.getString(3);

                    miWarehouseDbHelper.insertRecord(db, TABLENAME, BARCODEstring, DESCRIPTIONstring,
                            Integer.parseInt(QUANTITYstring), Integer.parseInt(PRICEstring));
                    getCashierDatatableToList(tableName);
                    initiatePopupCashierWindowOne();
                    cursorProductBarcodeChk.close();
                }
                else{
                    DESCRIPTIONstring = "";     QUANTITYstring = "1";       PRICEstring = "0";      PROMOTIONstring = "";

                    initiatePopupCashierProductInputWindow();
                    cursorProductBarcodeChk.close();
                }

            }
            cursorBarcodeChk.close();

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " Can't compare Barcode with Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

public void initiatePopupCashierWindowOne(){

        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Cashier.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_cashier_one);

        View layout = inflater.inflate(R.layout.popup_cashier_one, popupGroup);
        int customScreenHeight80 = (int) 8*screenHeight/10;
        popupCashierWindowOne = new PopupWindow(layout, screenWidth, customScreenHeight80, true);

        popupCashierWindowOne.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

        popupCashierNumber = (EditText) layout.findViewById(R.id.popup_cashier_number);
        //popupNumber.setText(QUANTITYstring);
        popupCashierNumber.requestFocus();

        //    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //    imm.showSoftInput(popupCashierNumber, InputMethodManager.SHOW_IMPLICIT);
        //  didn't work

        if ( "".equals(QUANTITYstring) | QUANTITYstring == null ){ QUANTITYstring = "0"; }

        popupCashierNumber.setText(QUANTITYstring);
        oldQUANTITYstring = QUANTITYstring;

    }

    public void finishPopupCashierWindowOne(View v){

        QUANTITYstring = popupCashierNumber.getEditableText().toString();

        if ( "".equals(QUANTITYstring) ){
            Toast toast = Toast.makeText(this, " Porfavor ingresa un Numero", Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            popupCashierWindowOne.dismiss();
            if("0".equals(oldQUANTITYstring) | ( oldQUANTITYstring == null ) )  {
                oldQUANTITYstring = "1";
                Cursor cursorPriceQuickGet = db.query( "PRODUCTS", new String[] {"BARCODE", "PRICE" },  "BARCODE = ?", new String[] {BARCODEstring},  null, null, null );
                cursorPriceQuickGet.moveToFirst();
                PRICEstring = cursorPriceQuickGet.getString(1);
                cursorPriceQuickGet.close();
            }

            int newPrice = Integer.parseInt(QUANTITYstring) * ( Integer.parseInt(PRICEstring) / Integer.parseInt(oldQUANTITYstring) );

            miWarehouseDbHelper.updateColumn(db, TABLENAME, BARCODEstring, "QUANTITY", QUANTITYstring,
                    "PRICE" , Integer.toString(newPrice));

            getCashierDatatableToList(TABLENAME);


        }
    }

                                                public void closePopupCashierWindowOne(View v){
                                                    popupCashierWindowOne.dismiss();
                                                }

                                                public void popupCashierAddOneInteger(View v){
                                                    String value = popupCashierNumber.getEditableText().toString();
                                                    if ( "".equals(value) ) {value = "0"; }

                                                    int added = Integer.parseInt(value) + 1;

                                                    popupCashierNumber.setText( Integer.toString(added) );
                                                }
                                                        public void popupCashierMinusOneInteger(View v){

                                                            String value = popupCashierNumber.getEditableText().toString();
                                                            if ( "".equals(value) ) {value = "0"; }

                                                            int added = Integer.parseInt(value) - 1;

                                                            popupCashierNumber.setText(Integer.toString(added));
                                                        }
public void initiatePopupCashierWindowTwo(View v){
    try{
        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Cashier.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_window_two);

         View layout = inflater.inflate(R.layout.popup_two_layout, popupGroup);

         popupCashierWindowTwo = new PopupWindow(layout, screenWidth, screenHeight, true);
         popupCashierWindowOne.dismiss();
         popupCashierWindowTwo.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

    } catch (Exception e){
           e.printStackTrace();
    }
}

    public void closePopupWindowTwo(View v){
        miWarehouseDbHelper.deleteRecord(db, TABLENAME, BARCODEstring);
        popupCashierWindowTwo.dismiss();
        getCashierDatatableToList(TABLENAME);
    }
                                                public void cancelPopupWindowTwo(View v){
                                                    popupCashierWindowTwo.dismiss();
                                                    initiatePopupCashierWindowOne();
                                                }


    public void initiatePopupCashierCustomItem(View v){

        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Cashier.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_cashier_custom_item_group);

        View layout = inflater.inflate(R.layout.popup_cashier_custom_item, popupGroup);
           int  customScreenHeight = (int) 8*screenHeight / 10 ;
        popupCashierCustomItemWindow = new PopupWindow(layout, screenWidth, customScreenHeight, true);

        popupCashierCustomItemWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

        popupCashierCustomItemBarcode       = (EditText) layout.findViewById(R.id.popup_custom_item_barcode);
        popupCashierCustomItemQuantity      = (EditText) layout.findViewById(R.id.popup_custom_item_quantity);
        popupCashierCustomItemPriceUnit     = (EditText) layout.findViewById(R.id.popup_custom_item_price_per_unit);
        popupCashierCustomItemDescription   = (EditText) layout.findViewById(R.id.popup_custom_item_product_name);
        popupCashierCustomItemTotal         = (EditText) layout.findViewById(R.id.popup_custom_item_price_total);


        popupCashierCustomItemPriceUnit.requestFocus();

        popupCashierCustomItemBarcode.setEnabled(false);

        Cursor cursorSizer = db.query( "CASHIER", new String[] { "BARCODE", "DESCRIPTION"},  null, null ,  null, null, null );
            int sizer = (int) cursorSizer.getCount();
            //if(sizer == null) { sizer = 0 };
            sizer++;
        popupCashierCustomItemBarcode.setText("A" + Integer.toString(sizer));

        popupCashierCustomItemPriceUnit.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){

                String value    = popupCashierCustomItemPriceUnit.getEditableText().toString();
                String quantity = popupCashierCustomItemQuantity.getEditableText().toString();

                if("".equals(value) | value == null)        {   value = "0";}
                if("".equals(quantity) | quantity == null)  {   quantity = "0";                }

                int price =  Integer.parseInt(value) * Integer.parseInt(quantity);

                popupCashierCustomItemTotal.setText(Integer.toString(price));
                return false;
            }
        } );

        popupCashierCustomItemQuantity.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                String quantity = popupCashierCustomItemQuantity.getEditableText().toString();
                String value    = popupCashierCustomItemPriceUnit.getEditableText().toString();

                if("".equals(value) | value == null)        {   value = "0";}
                if("".equals(quantity) | quantity == null)  {   quantity = "0";                }
                                                                                // Don't know how to deal with BACKSPACE key,  it doesn't detect when it gets pressed.
                                                                                //if(keyCode == KeyEvent.KEYCODE_DEL){
                                                                                //    value = popupCashierPaymentCustomerPays.getEditableText().toString();
                                                                                //    if( value == null | "".equals(value)) {value = "0";}
                                                                                //}

                int price =  Integer.parseInt(value) * Integer.parseInt(quantity);

                popupCashierCustomItemTotal.setText(Integer.toString(price));

                return false;
            }
        } );

    }

    public void finishPopupCashierCustomItemWindow(View v){

        QUANTITYstring          = popupCashierCustomItemQuantity.getEditableText().toString();
        String PRICEunitString  = popupCashierCustomItemPriceUnit.getEditableText().toString();
        String Description      = popupCashierCustomItemDescription.getEditableText().toString();
        String TOTALcostString  = popupCashierCustomItemTotal.getEditableText().toString();
        String CustomBarcodeStr = popupCashierCustomItemBarcode.getEditableText().toString();

        if ( "".equals(QUANTITYstring) | "".equals(PRICEunitString) | "".equals(TOTALcostString) ){
            Toast toast = Toast.makeText(this, " Porfavor ingresa un Numero", Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            if ( "0".equals(QUANTITYstring) | "0".equals(PRICEunitString) | "0".equals(TOTALcostString) ){
                Toast toast = Toast.makeText(this, " Porfavor ingresa un Numero", Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                popupCashierCustomItemWindow.dismiss();

                miWarehouseDbHelper.insertRecord(db, TABLENAME, CustomBarcodeStr , Description,
                        Integer.parseInt(QUANTITYstring), Integer.parseInt(TOTALcostString));

                getCashierDatatableToList(TABLENAME);
            }
        }
    }

    public void closePopupCashierCustomItemWindow (View v){
        popupCashierCustomItemWindow.dismiss();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////
    ////////////            Figure out dynamic listening when editing PRICEperUNIT box
    ////////////

    public void popupCashierCustomItemAddOneInteger(View v){
        String quantity     = popupCashierCustomItemQuantity.getEditableText().toString();
        String priceunit    = popupCashierCustomItemPriceUnit.getEditableText().toString();
        String total        = popupCashierCustomItemTotal.getEditableText().toString();

        if ( "".equals(quantity) ) {quantity = "0"; }
            int calculated;
        int added = Integer.parseInt(quantity) + 1;
        if(added < 0) { added = 0; }

        calculated = Integer.parseInt(priceunit) * added;

        popupCashierCustomItemQuantity.setText( Integer.toString(added) );
       // popupCashierCustomItemPriceUnit.setText( priceunit );
        popupCashierCustomItemTotal.setText( Integer.toString(calculated) );

    }
                                                    public void popupCashierCustomItemMinusOneInteger(View v){

                                                        String quantity     = popupCashierCustomItemQuantity.getEditableText().toString();
                                                        String priceunit    = popupCashierCustomItemPriceUnit.getEditableText().toString();
                                                        String total        = popupCashierCustomItemTotal.getEditableText().toString();

                                                        if ( "".equals(quantity) ) {quantity = "0"; }
                                                        int calculated;
                                                        int added = Integer.parseInt(quantity) - 1;
                                                            if(added < 0) { added = 0; }
                                                            calculated = Integer.parseInt(priceunit) * added;

                                                        popupCashierCustomItemQuantity.setText( Integer.toString(added) );
                                                        //popupCashierCustomItemPriceUnit.setText( priceunit );
                                                        popupCashierCustomItemTotal.setText( Integer.toString(calculated) );
                                                    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////
    ////////////////////////////

    public void initiatePopupCashierPaymentWindow(View v){

        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Cashier.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_cashier_payment_group);

        View layout = inflater.inflate(R.layout.popup_cashier_payment, popupGroup);

        popupCashierPaymentWindow = new PopupWindow(layout, screenWidth, screenHeight, true);

        popupCashierPaymentWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

        popupCashierPaymentTotal            = (TextView)    layout.findViewById(R.id.popup_cashier_payment_total);
        popupCashierPaymentCustomerPays     = (EditText)    layout.findViewById(R.id.popup_cashier_payment_customer_pays);
        popupCashierPaymentCustomerReceives = (TextView)    layout.findViewById(R.id.popup_cashier_payment_customer_receives);
        popupCashierPaymentUpdateButton     = (Button)      layout.findViewById(R.id.popup_cashier_payment_finishpopupwindow);

       // popupCashierPaymentCustomerPays.setOnKeyListener();
        if("".equals(TOTALpriceStr) | TOTALpriceStr == null) {TOTALpriceStr = "0";}
        popupCashierPaymentTotal.setText(TOTALpriceStr);
        if("0".equals(TOTALpriceStr) ) {
            popupCashierPaymentCustomerReceives.setText(TOTALpriceStr);
        }
        else {
            popupCashierPaymentCustomerReceives.setText("-" + TOTALpriceStr);
        }
            popupCashierPaymentUpdateButton.setBackgroundColor(0xFFCF0A0A);

        popupCashierPaymentCustomerPays.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){

                String value = popupCashierPaymentCustomerPays.getEditableText().toString();
                            if("".equals(value) | value == null) {value = "0";}
                if(keyCode == KeyEvent.KEYCODE_DEL){
                    value = popupCashierPaymentCustomerPays.getEditableText().toString();
                    if( value == null | "".equals(value)) {value = "0";}
                }
                if("".equals(TOTALpriceStr) | TOTALpriceStr == null) {TOTALpriceStr = "0";}
                int difference =  Integer.parseInt(value) - Integer.parseInt(TOTALpriceStr);

                if(difference <= 0) {
                    popupCashierPaymentCustomerReceives.setTextColor(0xFFCF0A0A);
                    popupCashierPaymentUpdateButton.setBackgroundColor(0xFFCF0A0A);
                    }
                else {
                    popupCashierPaymentCustomerReceives.setTextColor(0xFF159C08);
                    popupCashierPaymentUpdateButton.setBackgroundColor(0xFF159C08);
                }

                popupCashierPaymentCustomerReceives.setText( Integer.toString(difference) );
                return false;
            }
        } );
    }

    public void closePopupCashierPaymentWindow (View v){
        popupCashierPaymentWindow.dismiss();
    }


    public void finishPopupCashierPaymentWindow (View v) {
        String custPays = popupCashierPaymentCustomerPays.getEditableText().toString();
        String custRec = popupCashierPaymentCustomerReceives.getEditableText().toString();

        if ("".equals(custPays) | custPays == null)                 {   custPays = "0";             }
        if ("".equals(custRec)  | custRec == null)                  {   custRec = "0";             }
        if ("".equals(TOTALquantityStr) | TOTALquantityStr == null) {   TOTALquantityStr = "0";     }
        if ("".equals(TOTALpriceStr)    | TOTALpriceStr == null)    {   TOTALpriceStr = "0";        }

        int totalQ = Integer.parseInt(TOTALquantityStr);
        int totalP = Integer.parseInt(TOTALpriceStr);
        int customerPays = Integer.parseInt(custPays);
        int customerReceipts = Integer.parseInt(custRec);

        if ((totalQ == 0) & (totalP == 0) & (customerPays == 0) & (customerReceipts == 0)) {
            popupCashierPaymentWindow.dismiss();
        } else {
            if ((customerReceipts < 0) | (customerPays - totalP < 0)) {
                Toast toast = Toast.makeText(this, " Porfavor Revisa lo que paga el Cliente", Toast.LENGTH_LONG);
                toast.show();
            } else {
                miWarehouseDbHelper.insertSalesRecord(db, "HISTORYSALES", totalQ, totalP, customerPays, customerReceipts);

                //miWarehouseDbHelper.insertTableIntoHistoryCashier(db, "CASHIER", "HISTORYCASHIER");
                miWarehouseDbHelper.insertTableIntoCASHIERHISTORY(db, "CASHIER", "CASHIER_HISTORY");
                miWarehouseDbHelper.insertRecordAccount(db , "CASHIER_HISTORY" , "AAA0001" , "Cliente Paga" , 0, customerPays, 0);
                miWarehouseDbHelper.insertRecordAccount(db , "CASHIER_HISTORY" , "AAA0002" , "Vuelto" , 0, 0, customerReceipts );

                popupCashierPaymentWindow.dismiss();

                db.execSQL("DELETE FROM CASHIER WHERE BARCODE LIKE \'A%\'");

                //db.delete("CASHIER", "BARCODE=?", new String[] {"0000000000000"} );
                // db.execSQL("UPDATE CASHIER SET QUANTITY = ( SELECT (SHELVES.QUANTITY - CASHIER.QUANTITY) AS QUAN WHERE ) " +
                //         " WHERE  SHELVES.BARCODE = CASHIER.BARCODE ; ");
                //  HOW TO do it within SQLite??????

                Cursor cursorCashierTable = db.query("CASHIER", new String[]{"BARCODE", "QUANTITY", "DESCRIPTION", "PRICE"}, null, null, null, null, "BARCODE ASC");
                if (cursorCashierTable.moveToFirst()) {
                    int CASHIERSIZE = cursorCashierTable.getCount();

                    for (int i = 0; i < CASHIERSIZE; i++) {
                        cursorCashierTable.moveToPosition(i);
                        int quantCashier = cursorCashierTable.getInt(1);

                        String barcode = cursorCashierTable.getString(0);
                        String description = cursorCashierTable.getString(2);
                        String price = cursorCashierTable.getString(3);
                        if ("".equals(price) | price == null) {
                            price = "0";
                        }

                        Cursor cursorShelvesTable = db.query("SHELVES", new String[]{"BARCODE", "QUANTITY"}, "BARCODE = ?", new String[]{barcode}, null, null, null);

                        if (cursorShelvesTable.moveToFirst()) {
                            int quantShelves = cursorShelvesTable.getInt(1);
                            int difference = quantShelves - quantCashier;

                            miWarehouseDbHelper.updateColumn(db, "SHELVES", barcode, "QUANTITY", Integer.toString(difference),
                                    "DESCRIPTION", description, "PRICE", price);
                        } else {
                            quantCashier = (-1) * quantCashier;

                            miWarehouseDbHelper.insertRecord(db, "SHELVES", barcode, description, quantCashier, Integer.parseInt(price));
                        }
                        cursorShelvesTable.close();
                    }

                    db.delete("CASHIER", null, null);
                    cursorCashierTable.close();

                } else {
                    // NOT sure what kind of error to expect????
                }

                getCashierDatatableToList("CASHIER");
            }
        }
    }


    public void initiatePopupCashierProductInputWindow(){

        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Cashier.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_product_info_input_group);

        View layout = inflater.inflate(R.layout.popup_product_info_input, popupGroup);

        popupProductWindow = new PopupWindow(layout, screenWidth, screenHeight, true);

        popupProductWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

        popupProductBarcode             = (EditText) layout.findViewById(R.id.popup_product_info_barcode);
        popupProductDescription         = (EditText) layout.findViewById(R.id.popup_product_info_description);
        popupProductPrice               = (EditText) layout.findViewById(R.id.popup_product_info_price);
        popupProductPromotionCheckbox   = (CheckBox) layout.findViewById(R.id.popup_product_info_promotion_checkbox);
        popupProductEraseRecordButton   = (Button)      layout.findViewById(R.id.popup_product_info_delete_record);
        popupProductBarcodeTextTitle    = (TextView)    layout.findViewById(R.id.popup_product_info_textview1);

        popupProductEraseRecordButton.setEnabled(false);

        popupProductBarcode.setText(BARCODEstring);
        popupProductDescription.setText(DESCRIPTIONstring);
        popupProductPrice.setText(PRICEstring);

        popupProductBarcode.setEnabled(false);

        popupProductBarcodeTextTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick( View view) {
                popupProductBarcode.setEnabled(true);
                return true;
            }
        });

        if("".equals(PRICEstring)) { PRICEstring = "0";}
        if("oferta".equals(PROMOTIONstring)) {
            popupProductPromotionCheckbox.setChecked(true);
            popupProductPrice.setTextColor(0xFFC10000);
        }
        else{
            popupProductPromotionCheckbox.setChecked(false);
            popupProductPrice.setTextColor(0xFF000000);
        }
    }

    public void finishPopupProductsWindow(View v){
        BARCODEstring       = popupProductBarcode.getEditableText().toString();
        DESCRIPTIONstring   = popupProductDescription.getEditableText().toString();
        PRICEstring         = popupProductPrice.getEditableText().toString();
        CATEGORYstring      = "nothing";

        if(popupProductPromotionCheckbox.isChecked())   { PROMOTIONstring = "oferta"; }
        else    { PROMOTIONstring = ""; }

        if("".equals(PRICEstring) | "".equals(DESCRIPTIONstring) | "".equals(BARCODEstring) ){
            Toast toast = Toast.makeText(this, " Por favor revisa los datos", Toast.LENGTH_LONG);
            toast.show();
        }
        else{

            Cursor cursorBarcodeChk = db.query( TABLENAME, new String[] {"BARCODE"}
                    ,  "BARCODE = ?", new String[] {BARCODEstring},  null, null, null );

            if(cursorBarcodeChk.moveToFirst()){
                miWarehouseDbHelper.updateColumn(db, "PRODUCTS", BARCODEstring, "DESCRIPTION" , DESCRIPTIONstring,
                        "PRICE" , PRICEstring, "CATEGORY", CATEGORYstring, "PROMOTION", PROMOTIONstring);
            }
            else {
                miWarehouseDbHelper.insertProductsRecord(db, "PRODUCTS", BARCODEstring, DESCRIPTIONstring, "0",
                        PRICEstring, CATEGORYstring, PROMOTIONstring);
            }
            popupProductWindow.dismiss();
            getCashierDatatableToList(TABLENAME);
            chkCashierBarcodeToDatatable(TABLENAME, BARCODEstring);
            cursorBarcodeChk.close();
        }
    }

    public void cancelPopupProductsWindow(View v){
        popupProductWindow.dismiss();
    }

}