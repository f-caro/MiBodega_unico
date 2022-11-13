package com.ostachio.mibodega_unico;

import android.os.Bundle;
import android.app.Activity;

import android.content.Intent;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import android.widget.AdapterView.*;
import android.widget.ListView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.CursorAdapter;
import android.widget.PopupWindow;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class Add_Product_to_Storage extends Activity {

    private ImageButton scanBtn;
    private TextView formatTxt, contentTxt;
    private ListView listViewProducts;
    private EditText popupNumber;
    private GridView gridViewProducts;
    private SearchView searchList;

    private String  TABLENAME,      BARCODEstring,    STORAGEquantityStr, oldQUANTITYstring;
    private String  QUANTITYstring, PRICEstring,    DESCRIPTIONstring ,     PROMOTIONstring,    CATEGORYstring;
    private int screenWidth, screenHeight;

    private int PRODUCT_QUANTITY, PRODUCT_PRICE;

    private PopupWindow popupWindowOne, popupWindowTwo;


    private SQLiteDatabase db;
    private Cursor cursorBarcodeChk, cursorListProducts, cursorQuantityChk ;

    private PopupWindow popupProductWindow;
                private EditText    popupProductBarcode, popupProductDescription, popupProductPrice;
                private CheckBox    popupProductPromotionCheckbox;
                private Button      popupProductEraseRecordButton;
                private TextView    popupProductBarcodeTextTitle;

    // final Handler timerHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__product_to__storage);
        Intent from_main_screen = getIntent();

        scanBtn = (ImageButton) findViewById(R.id.button_storage_scan);
        //formatTxt = (TextView) findViewById(R.id.storage_barcode_type);
        contentTxt = (TextView) findViewById(R.id.storage_barcode);
        // popupNumber = (EditText) findViewById(R.id.popup_number);

        listViewProducts = (ListView) findViewById(R.id.list_view_products);
        searchList = (SearchView) findViewById(R.id.searchview_storage);

        listViewProducts.setOnItemClickListener(
                new OnItemClickListener(){
                    @Override

                    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id){
                       //String scanNumber =  listViewProducts.getItemAtPosition(position);  // <-- didn't work  ClassCast error

                       //cursorAdapterProducts.moveToPosition(position);

                         Cursor cur = (Cursor) listViewProducts.getItemAtPosition(position); //cursorListProducts.getString(1);        // (String) parent.getItemAtPosition(position);
                            cur.moveToPosition(position);

                        BARCODEstring = cur.getString(1);
                        DESCRIPTIONstring = cur.getString(2);  // <-- cursor is not the one you thought it was .: QUANTITYstring ends up NULL
                        QUANTITYstring = cur.getString(3);
                                /////  All okay....  2016 - Aug - 09 :-)
                        //Toast toast = Toast.makeText(getApplicationContext(), BARCODEstring + " ---  " + DESCRIPTIONstring + " --- "
                        //        + QUANTITYstring , Toast.LENGTH_SHORT);
                        // toast.show();

                        initiatePopupWindowOne();
                       // return true;
                    }

        }        );

        //gridViewProducts = (GridView) findViewById(R.id.grid_view_products);
        TABLENAME =  from_main_screen.getStringExtra("tablename"); //"STORAGE"  or  "SHELVES"  names of tables;
                if("STORAGE".equals(TABLENAME)) {
                    contentTxt.setText("Lista de Productos en la Bodega");
                    contentTxt.setBackgroundColor(0xFF5C8339);
                    contentTxt.setTextColor(0xFFFFFFFF);
                };
                if("SHELVES".equals(TABLENAME)) {
                    contentTxt.setText("Lista de Productos en la Vetrina");
                    contentTxt.setBackgroundColor(0xFF379C8D);
                    contentTxt.setTextColor(0xFFFFFFFF);
                };

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        screenHeight    = metrics.heightPixels - 80;
        // int screenHeight60 = (int) 6*screenHeight/10;   // <-- Math trickshot to get 60% of screenHeight
        screenWidth     = metrics.widthPixels;

        getDatatableToList(TABLENAME);

        searchList.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String queryText) {
                        Toast.makeText(getApplicationContext(), queryText + " \n " , Toast.LENGTH_SHORT).show();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String dynamicText){
                        //Toast.makeText(getApplicationContext(), dynamicText + " \n ", Toast.LENGTH_SHORT).show();
                        String queryString = "SELECT _id, DESCRIPTION FROM " + TABLENAME +  " WHERE DESCRIPTION LIKE \'" + dynamicText + "%\' LIMIT 5 ";

                        Cursor cursorSearchProducts = db.rawQuery(queryString, null);

                        // connecting SimpleCursorAdapter with simple layout
                        CursorAdapter cursorSearchAdapter =  new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_search_suggestion_layout ,
                                cursorSearchProducts , new String[] { "DESCRIPTION" },
                                new int[] { R.id.custom_search_suggestion_description} , 0);

                        // connecting custom Adapter with Products ListView
                        searchList.setSuggestionsAdapter(cursorSearchAdapter);
                        //cursorSearchProducts.close();

                        return false;
                    }
                });

        searchList.setOnSuggestionListener(
                new SearchView.OnSuggestionListener(){
         @Override
         public boolean onSuggestionClick(int position){
              Cursor cur =  (Cursor) searchList.getSuggestionsAdapter().getCursor();
              cur.moveToPosition(position);
              String queryString = cur.getString(1);  //  --- NB.  cur.getString(0) --- holds _id number or something else.... but gives back integer.

              searchList.setQuery(queryString, true);
              getSearchDataToList(queryString);
              //Toast.makeText(getApplicationContext(), queryString + " \n " , Toast.LENGTH_SHORT).show();
              return true;
              }

              @Override
              public boolean onSuggestionSelect(int position){
                  return false;
                  }
          }
        );

        searchList.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose(){
                getDatatableToList(TABLENAME);
                return true;
            }
        });
        //scanBtn.setOnClickListener(this);
    }

    { /*
                                            public void onClick(View v){
                                                if(v.getId()==R.id.button_storage_scan ){
                                                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                                                    scanIntegrator.initiateScan();
                                                }
                                            }
                                         */
    }

    public void startScan(View v){
        if(v.getId()==R.id.button_storage_scan ) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null){
            BARCODEstring = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            if( "".equals(BARCODEstring) | BARCODEstring == null ) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No Scan Data Recibido", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                // quick unit test:::::
                // BARCODEstring = "05273652";   // <-- works like a charm :-)
                int barcodeSize = BARCODEstring.length();
                if ( ( barcodeSize != 13 ) & (barcodeSize != 8) ) {
                    Toast toastRepeatScan = Toast.makeText(getApplicationContext(),
                            "Error during Scan, pls Try Scan Again", Toast.LENGTH_SHORT);
                    toastRepeatScan.show();
                    ///////////////////////////  Don't know if this works,  <--- works like charm :-)
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                    ///////////////////////////
                } else {
                    // formatTxt.setText("Format: " + scanFormat);
                    // contentTxt.setText("Barcode: " + BARCODEstring);
                    //   timerHandler.postDelayed(this, 800);   // <--- did't work ,  why??? :-(

                    chkBarcodeToDatatable(TABLENAME, BARCODEstring);
                    // getDatatableToList(TABLENAME);
                }
            }
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No Scan Data Recibido", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

  // Crashed the APP,  think it has to be placed only in the 1st MainActivity.java

    @Override
    public void onDestroy(){
        super.onDestroy();
     //   db.close();
     //   cursorBarcodeChk.close();
     //   cursorListProducts.close();
     //   cursorQuantityChk.close();
    }

    public void getDatatableToList (String tableName){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // connecting to a CursorAdapter and ListView widget
            Cursor cursorListProducts = db.query(tableName,
                    new String[] {"_id", "BARCODE" , "DESCRIPTION" , "QUANTITY" , "PRICE", "TIME_STAMPING" },
                    null, null, null, null, "TIME_STAMPING DESC");

                                        //ListView listViewProducts = getListView();
                                        // SimpleCursorAdapter connected to ListView only has max 2 lines per text.
                                        // try other layouts  <-- need to customize viewBind etc...   urrrrgggggghhhhhh

            // NOPE!!!!!!!!   just adjust SimpleCursorAdapter( ... inputs ...)   connected to custom_list_layout.xml
            // and it will do the rest!!!!!

            CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_list_layout ,
                    cursorListProducts , new String[] {"BARCODE", "QUANTITY", "DESCRIPTION"},
                    new int[] {R.id.row_text_1, R.id.row_text_2, R.id.row_text_3 } , 0);
                    //  modified by  changing  simple_list_item_1 --> simple_list_item_2
            listViewProducts.setAdapter(cursorAdapterProducts);
                                        /*
                                        // GridView attempt with SimpleCursorAdapter.
                                        CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1 ,
                                                cursorListProducts , new String[] {"BARCODE", "TIME_STAMPING"},
                                                new int[] {android.R.id.text1, android.R.id.text1 } , 0);
                                        gridViewProducts.setAdapter(cursorAdapterProducts);
                                        //  displays same entries as a grid,  with same mistakes as Text1 vs Text2.
                                        // same difference --- eish.
                                        */

            // DON't  CLOSE cursor in this Method!!!!!!!
            //  cursorAdapterProducts   HAS to  CONNECT  to  ListView widget!!!!!!

        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " Can't read from Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void chkBarcodeToDatatable(String tableName, String scanContent){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // first check the barcode with PRODUCTS table entry

            Cursor cursorProductBarcodeChk = db.query( "PRODUCTS", new String[] {"BARCODE", "DESCRIPTION" , "QUANTITY", "PRICE", "CATEGORY", "PROMOTION"} ,  "BARCODE = ?",
                    new String[] {scanContent},  null, null, null );
            if(cursorProductBarcodeChk.moveToFirst() == false) {
                DESCRIPTIONstring = "";     QUANTITYstring = "0";       PRICEstring = "0";      PROMOTIONstring = "";
                initiatePopupStorageShelvesProductInputWindow();
                cursorProductBarcodeChk.close();
            }
            else {

                DESCRIPTIONstring = cursorProductBarcodeChk.getString(1);
                QUANTITYstring = "0";
                PRICEstring = cursorProductBarcodeChk.getString(3);
                PROMOTIONstring = cursorProductBarcodeChk.getString(4);

                Cursor cursorBarcodeChk = db.query(tableName, new String[]{"BARCODE", "DESCRIPTION", "QUANTITY", "PRICE"}, "BARCODE = ?",
                        new String[]{scanContent}, null, null, null);

                if (cursorBarcodeChk.moveToFirst()) {                               // // get entry from CURRENT  table  aka TABLENAME constant
                    QUANTITYstring = cursorBarcodeChk.getString(2);

                    if ("SHELVES".equals(tableName)) {                              // // get entry from STORAGE  table  to compare
                        Cursor cursorStorageChk = db.query("STORAGE", new String[]{"BARCODE", "QUANTITY"}, "BARCODE = ?",
                                new String[]{scanContent}, null, null, null);

                        if (cursorStorageChk.moveToFirst()) {
                            STORAGEquantityStr = cursorStorageChk.getString(1);
                        } else {
                            miWarehouseDbHelper.insertRecord(db, "STORAGE", scanContent, DESCRIPTIONstring, 0, Integer.parseInt(PRICEstring) );
                            STORAGEquantityStr = "0";
                        }
                        cursorStorageChk.close();
                    }
                    initiatePopupWindowOne();
                    // miWarehouseDbHelper.updateColumn(db , tableName , scanContent,
                    //         "QUANTITY",  Integer.toString(PRODUCT_QUANTITY)  );     // <--- remember QUANTITY is table Column name !!!!
                }
                else {
                    miWarehouseDbHelper.insertRecord(db, TABLENAME, scanContent, DESCRIPTIONstring, 0, Integer.parseInt(PRICEstring) );
                    getDatatableToList(TABLENAME);
                    QUANTITYstring = "0";

                    if ("SHELVES".equals(tableName)) {
                        Cursor cursorStorageChk = db.query("STORAGE", new String[]{"BARCODE", "QUANTITY"}, "BARCODE = ?",
                                new String[]{scanContent}, null, null, null);

                        if (cursorStorageChk.moveToFirst()) {
                            STORAGEquantityStr = cursorStorageChk.getString(1);
                        } else {
                            miWarehouseDbHelper.insertRecord(db, "STORAGE", scanContent, DESCRIPTIONstring, 0, Integer.parseInt(PRICEstring) );
                            STORAGEquantityStr = "0";
                        }
                        cursorStorageChk.close();
                    }
                    initiatePopupWindowOne();
                }
            cursorBarcodeChk.close();

                // quick test to see the changes ---

                //Cursor cursorQuantityChk = db.query(tableName, new String[] {"QUANTITY"},  "BARCODE = ?",
                //        new String[] {scanContent},  null, null, null );
                // if(cursorQuantityChk.moveToFirst()){
                //   formatTxt.setText("Quantity:  " + QUANTITYstring ); // cursorQuantityChk.getInt(0));
            }
        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " Can't compare Barcode with Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void getSearchDataToList (String searchProduct){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // connecting to a CursorAdapter
            Cursor cursorListProducts = db.query(TABLENAME,
                    new String[] {"_id", "BARCODE" , "DESCRIPTION" ,"QUANTITY", "PRICE", "TIME_STAMPING" },
                    "DESCRIPTION = ?", new String[] { searchProduct }, null, null, "DESCRIPTION ASC");


            // connecting SimpleCursorAdapter with custom layout
            CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_list_layout ,
                    cursorListProducts , new String[] {"BARCODE", "QUANTITY", "DESCRIPTION"},
                    new int[] {R.id.row_text_1, R.id.row_text_2, R.id.row_text_3 } , 0);

            // connecting custom Adapter with Products ListView
            listViewProducts.setAdapter(cursorAdapterProducts);


        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " No se puede leer del Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //              POPUP window ONE
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initiatePopupWindowOne(){
      // try{
        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Storage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_window_one);

        View layout = inflater.inflate(R.layout.popup_one_layout, popupGroup);

        popupWindowOne = new PopupWindow(layout, screenWidth, screenHeight, true);
                                   //  popupWindowOne.setHeight(WindowManager.LayoutParams.FILL_PARENT);
                                   //  popupWindowOne.setWidth();

        popupWindowOne.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // ?????  hoooowwwww??????     getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        popupNumber = (EditText) layout.findViewById(R.id.popup_number);
        popupNumber.requestFocus();

        if ( "".equals(QUANTITYstring) ){ QUANTITYstring = "0"; }
        popupNumber.setText(QUANTITYstring);
        oldQUANTITYstring = QUANTITYstring;

        //popupNumber.requestFocusFromTouch();
        //popupNumber.setShowSoftInputOnFocus(true); // < --- need API21 ++ //
        // } catch (Exception e){
        //     e.printStackTrace();
        // }
    }

    public void closePopupWindowOne(View v){

        QUANTITYstring = popupNumber.getEditableText().toString();

        if ( "".equals(QUANTITYstring) ){
            Toast toast = Toast.makeText(this, " Porfavor ingresa un Numero", Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            popupWindowOne.dismiss();
            miWarehouseDbHelper.updateColumn(db, TABLENAME, BARCODEstring, "QUANTITY", QUANTITYstring, "DESCRIPTION", DESCRIPTIONstring, "PRICE" , PRICEstring);

                if("SHELVES".equals(TABLENAME)) {
                 // if("".equals("STORAGEquantityStr") | STORAGEquantityStr == null) )
                    {
                        Cursor cursorStorageChk = db.query("STORAGE", new String[]{"BARCODE", "QUANTITY"}, "BARCODE = ?",
                                new String[]{BARCODEstring}, null, null, null);

                        if (cursorStorageChk.moveToFirst()) {
                            STORAGEquantityStr = cursorStorageChk.getString(1);
                        } else {
                            if("".equals(PRICEstring) | (PRICEstring == null)  ) { PRICEstring = "0"; }
                            miWarehouseDbHelper.insertRecord(db, "STORAGE", BARCODEstring, DESCRIPTIONstring, 0, Integer.parseInt(PRICEstring) );
                            STORAGEquantityStr = "0";
                        }
                        cursorStorageChk.close();
                    }

                    if( (STORAGEquantityStr == null ) | ("".equals(STORAGEquantityStr)) ) { STORAGEquantityStr = "0";}

                            int        storageQ = Integer.parseInt(STORAGEquantityStr);
                            int  updateShelvesQ = Integer.parseInt(QUANTITYstring);
                            int     oldShelvesQ = Integer.parseInt(oldQUANTITYstring);

                    int difference = oldShelvesQ + storageQ - updateShelvesQ;

                    miWarehouseDbHelper.updateColumn(db, "STORAGE", BARCODEstring, "QUANTITY", Integer.toString(difference) );

                    Toast toast = Toast.makeText(this, "Bodega("+ STORAGEquantityStr + ") - Vetrina(" + QUANTITYstring + ") = "
                                + Integer.toString(difference) + " ::: Total en Bodega"
                                , Toast.LENGTH_LONG);
                    toast.show();
                }

            //formatTxt.setText("Format: " + QUANTITYstring);
            //contentTxt.setText("Barcode: " + BARCODEstring);
            getDatatableToList(TABLENAME);
        }
    }

    public void cancelPopupWindowOne(View v){        popupWindowOne.dismiss();    }
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //              POPUP window TWO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initiatePopupWindowTwo(View v){
         try{
        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Storage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_window_two);

        View layout = inflater.inflate(R.layout.popup_two_layout, popupGroup);

        popupWindowTwo = new PopupWindow(layout, screenWidth, screenHeight, true);

             popupWindowTwo.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

             popupWindowOne.dismiss();

         } catch (Exception e){
             e.printStackTrace();
          }
    }
    public void closePopupWindowTwo(View v){
        miWarehouseDbHelper.deleteRecord(db, TABLENAME, BARCODEstring);
        popupWindowTwo.dismiss();
        getDatatableToList(TABLENAME);
    }

                                            public void cancelPopupWindowTwo(View v){
                                                popupWindowTwo.dismiss();
                                                initiatePopupWindowOne();
                                            }
                                            public void popupAddOneInteger(View v){
                                                String value = popupNumber.getEditableText().toString();
                                                if ( "".equals(value) ) {value = "0"; }

                                                int added = Integer.parseInt(value) + 1;

                                                popupNumber.setText( Integer.toString(added) );
                                            }
                                                        public void popupMinusOneInteger(View v){

                                                            String value = popupNumber.getEditableText().toString();
                                                            if ( "".equals(value) ) {value = "0"; }

                                                            int added = Integer.parseInt(value) - 1;

                                                            popupNumber.setText(Integer.toString(added));
                                                        }
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //              POPUP window PRODUCTS input
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initiatePopupStorageShelvesProductInputWindow(){

        LayoutInflater inflater = (LayoutInflater) Add_Product_to_Storage.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        popupProductBarcode.setEnabled(false);

        popupProductBarcodeTextTitle.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick( View view) {
                            popupProductBarcode.setEnabled(true);
                            return true;
                        }
        });


        popupProductDescription.setText(DESCRIPTIONstring);
        popupProductPrice.setText(PRICEstring);

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
            getDatatableToList(TABLENAME);
            chkBarcodeToDatatable(TABLENAME, BARCODEstring);
            cursorBarcodeChk.close();
        }
    }
    public void cancelPopupProductsWindow(View v){          popupProductWindow.dismiss();    }
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
