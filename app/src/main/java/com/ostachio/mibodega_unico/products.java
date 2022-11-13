package com.ostachio.mibodega_unico;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.ImageView;
import android.widget.Button;
import android.util.DisplayMetrics;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class products extends Activity {

    private String TABLENAME = "PRODUCTS", BARCODEstring, DESCRIPTIONstring, CATEGORYstring, PRICEstring, PROMOTIONstring;
    private int screenWidth, screenHeight;
    private SQLiteDatabase db;

    private ListView productsListView;
    private SearchView searchProductList;

    private PopupWindow popupProductWindow, popupProductDeleteRecordWindow;

            private EditText    popupProductBarcode, popupProductDescription, popupProductPrice, popupProductImageAddress;
            private CheckBox    popupProductPromotionCheckbox;
            private TextView    popupProductBarcodeTextTitle;
            private ImageView   popupProductImage;
            private Button      popupProductButtonGetPhoto, popupProductButtonSendToWhatsApp;
            private Uri         imageUriInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        productsListView = (ListView) findViewById(R.id.product_listview);
        searchProductList = (SearchView) findViewById(R.id.searchview_product_search_list);

        getProductsDatatableToList(TABLENAME);

        productsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override

                    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id){

                        Cursor cur = (Cursor) productsListView.getItemAtPosition(position);
                        cur.moveToPosition(position);

                        BARCODEstring       = cur.getString(1);
                        DESCRIPTIONstring   = cur.getString(2);
                        PRICEstring         = cur.getString(3);
                        PROMOTIONstring     = cur.getString(6);

                        //Toast toast = Toast.makeText(getApplicationContext(), BARCODEstring + " ---  " + DESCRIPTIONstring + " --- "
                        //        + "---" + PRICEstring +"---" + PROMOTIONstring , Toast.LENGTH_SHORT);
                        // toast.show();

                         initiatePopupProductsWindow();

                        //return true;
                    }

                }        );

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        screenHeight    = metrics.heightPixels - 80;
        screenWidth     = metrics.widthPixels;

        searchProductList.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String queryText) {
                        Toast.makeText(getApplicationContext(), queryText + " \n " , Toast.LENGTH_SHORT).show();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String dynamicText){
                        //Toast.makeText(getApplicationContext(), dynamicText + " \n ", Toast.LENGTH_SHORT).show();
                         String queryString = "SELECT _id, DESCRIPTION FROM PRODUCTS WHERE DESCRIPTION LIKE \'" + dynamicText + "%\' LIMIT 5 ";

                        Cursor cursorSearchProducts = db.rawQuery(queryString, null);

                        /* Didn't work,  needed to design Cursor class connected to  db.querySomething ....   db.query doesn't handle  LIKE statements.
                                db.execSQL  didn't connect properly!!!!???? why???
                                db.rawQuery  worked !!!!!

                        Cursor cursorSearchProducts = db.query( "PRODUCTS", new String[] {"_id", "DESCRIPTION"}, "DESCRIPTION LIKE ? ",
                               new String[] { dynamicText }, null, null, "DESCRIPTION ASC");

                                db.execSQL(queryString);
                                db.query( "PRODUCTS", new String[] {"_id", "DESCRIPTION"}, null, null, null, null, "PROMOTION DESC, DESCRIPTION ASC");
                        */

                        // connecting SimpleCursorAdapter with simple layout
                        CursorAdapter cursorSearchAdapter =  new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_search_suggestion_layout ,
                                        cursorSearchProducts , new String[] { "DESCRIPTION" },
                                  new int[] { R.id.custom_search_suggestion_description} , 0);

                        // connecting custom Adapter with Products ListView
                        searchProductList.setSuggestionsAdapter(cursorSearchAdapter);
                        //cursorSearchProducts.close();

                        return false;
                    }
        });
        searchProductList.setOnSuggestionListener(new SearchView.OnSuggestionListener(){
            @Override
            public boolean onSuggestionClick(int position){
                Cursor cur =  (Cursor) searchProductList.getSuggestionsAdapter().getCursor();
                cur.moveToPosition(position);
                String queryString = cur.getString(1);  //  --- NB.  cur.getString(0) --- holds _id number or something else.... but gives back integer.

                searchProductList.setQuery(queryString, true);
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

        searchProductList.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose(){
                getProductsDatatableToList("PRODUCTS");
                return true;
            }
        });

    }

    public void startScan(View v){
      /*  try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] cam_list = manager.getCameraIdList();
            manager.setTorchMode("0", true);
        } catch(CameraAccessException c){
            Toast toast = Toast.makeText(this, c + " No hay Torch", Toast.LENGTH_LONG);
            toast.show();
        }
        */
        /*
        Camera cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        CameraConfigurationUtils.setTorch( p , true);
        */

        IntentIntegrator scanIntegrator = new IntentIntegrator(this);

        scanIntegrator.initiateScan();
    }

    static final int REQUESTCODE_THUMBNAIL_FROM_CAMERA = 2;
    static final int REQUESTCODE_PICTURE_FROM_GALLERY = 3;
    static final int REQUESTCODE_BARCODE_FROM_INTENTINTEGRATOR = 49374;
    static final int REQUESTCODE_FROM_WHATSAPP = 4;
    static final int REQUESTCODE_FULL_PICTURE = 5;

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    switch (requestCode) {
        case REQUESTCODE_THUMBNAIL_FROM_CAMERA :
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Se cancelo la foto?", Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == Activity.RESULT_OK) {
                Bitmap cameraPic = (Bitmap) intent.getExtras().get("data");
                String codeFilename = popupProductBarcode.getEditableText().toString();
                saveImageToInternalStorage(cameraPic, codeFilename);
                cameraPic = (Bitmap) readImageFromInternalStorage(codeFilename);
                popupProductImage.setAdjustViewBounds(true);
                popupProductImage.setScaleType(ImageView.ScaleType.FIT_XY);
                popupProductImage.setImageBitmap(cameraPic);
                popupProductImageAddress.setText( imageUriInfo.getLastPathSegment().toString() );

                                                                                                //  Previous code to work within  Internal Storage files
                                                                                                //Toast toast = Toast.makeText(getApplicationContext(),
                                                                                                //        "Se tomo una FOTO!!!", Toast.LENGTH_SHORT);
                                                                                                //toast.show();

                                                                                                //saveImageToInternalStorage(cameraPic, codeFilename);
                                                                                                //String fullcodeFilename = codeFilename + ".jpg";
                                                                                                //imageUriInfo = Uri.fromFile(new File(getFilesDir() , fullcodeFilename ) );
                                                                                                //popupProductImageAddress.setText( imageUriInfo.getLastPathSegment().toString() );
            }
            break;

        case REQUESTCODE_PICTURE_FROM_GALLERY :
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No eligio una Foto?", Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == Activity.RESULT_OK){

            }
            break;
        case REQUESTCODE_FROM_WHATSAPP :
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No mando a WhatsApp?", Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == Activity.RESULT_OK){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Se mando con exito.", Toast.LENGTH_SHORT);
                toast.show();
            }
            break;

        case REQUESTCODE_FULL_PICTURE :
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No tomo la Foto?", Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == Activity.RESULT_OK){

                String codeFilename = popupProductBarcode.getEditableText().toString();

                Bitmap cameraPic = (Bitmap) readFullImageFromExternalStorage(codeFilename);

                popupProductImage.setImageBitmap(cameraPic);
                popupProductImageAddress.setText( imageUriInfo.getLastPathSegment().toString() );
            }
            break;


        case REQUESTCODE_BARCODE_FROM_INTENTINTEGRATOR :
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "El codigo no fue eScaniado", Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == Activity.RESULT_OK) {
                IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

                if (scanningResult != null) {
                    BARCODEstring = scanningResult.getContents();

                    if ("".equals(BARCODEstring) | BARCODEstring == null) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No Scan Data Recibido", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        int barcodeSize = BARCODEstring.length();
                        if ((barcodeSize != 13) & (barcodeSize != 8)) {
                            Toast toastRepeatScan = Toast.makeText(getApplicationContext(),
                                    "Error durante Scan, Intenta denuevo", Toast.LENGTH_SHORT);
                            toastRepeatScan.show();

                            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                            scanIntegrator.initiateScan();
                        } else {
                            chkProductsBarcodeToDatatable(TABLENAME, BARCODEstring);
                        }
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No Scan Data Recibido", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } break;
    }
    }

    public void getProductsDatatableToList (String tableName){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // connecting to a CursorAdapter
            Cursor cursorListProducts = db.query(tableName,
                    new String[] {"_id", "BARCODE" , "DESCRIPTION" , "PRICE", "TIME_STAMPING", "CATEGORY", "PROMOTION" },
                    null, null, null, null, "PROMOTION DESC, DESCRIPTION ASC");

            // connecting SimpleCursorAdapter with custom layout
            CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_list_product_layout ,
                    cursorListProducts , new String[] {"BARCODE", "DESCRIPTION", "PRICE", "PROMOTION" },
                    new int[] { R.id.custom_list_product_barcode,   R.id.custom_list_product_description,
                                R.id.custom_list_product_price,     R.id.custom_list_product_promotion} , 0);

            // connecting custom Adapter with Products ListView
            productsListView.setAdapter(cursorAdapterProducts);


        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " No se puede leer del Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void chkProductsBarcodeToDatatable(String tableName, String scanContent){
        try{
            SQLiteOpenHelper miWarehouseDb = new miWarehouseDbHelper(this);
            db = miWarehouseDb.getReadableDatabase();

            // first check the barcode with old table entry  // // get entry from CASHIER table

            Cursor cursorBarcodeChk = db.query( tableName, new String[] {"BARCODE", "DESCRIPTION", "PRICE", "CATEGORY", "PROMOTION"},  "BARCODE = ?",
                    new String[] {scanContent},  null, null, null );

            if(cursorBarcodeChk.moveToFirst()){
                DESCRIPTIONstring   = cursorBarcodeChk.getString(1);
                PRICEstring         = cursorBarcodeChk.getString(2);
                CATEGORYstring      = cursorBarcodeChk.getString(3);
                PROMOTIONstring     = cursorBarcodeChk.getString(4);

                initiatePopupProductsWindow();

                // Toast toast = Toast.makeText(this," Value of QUANTITYstring = " + QUANTITYstring , Toast.LENGTH_LONG);
                // toast.show();
            }
            else {
                DESCRIPTIONstring   = "";
                PRICEstring         = "0";
                CATEGORYstring      = "";

                initiatePopupProductsWindow();
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
            Cursor cursorListProducts = db.query("PRODUCTS",
                    new String[] {"_id", "BARCODE" , "DESCRIPTION" , "PRICE", "TIME_STAMPING", "CATEGORY", "PROMOTION" },
                            "DESCRIPTION = ?", new String[] { searchProduct }, null, null, "DESCRIPTION ASC");


            // connecting SimpleCursorAdapter with custom layout
            CursorAdapter cursorAdapterProducts = new SimpleCursorAdapter(getApplicationContext(), R.layout.custom_list_product_layout ,
                    cursorListProducts , new String[] {"BARCODE", "DESCRIPTION", "PRICE", "PROMOTION" },
                    new int[] { R.id.custom_list_product_barcode,   R.id.custom_list_product_description,
                            R.id.custom_list_product_price,     R.id.custom_list_product_promotion} , 0);

            // connecting custom Adapter with Products ListView
            productsListView.setAdapter(cursorAdapterProducts);


        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, e + " No se puede leer del Database", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void sendToWhatsAPP(View v) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
/////////////////////////////////////////////   DOEsn't WORK in  BB OS.10  with  Android Emulation.....
        if(app_installed == true) {
            Cursor cursorProductPromotionsGet = db.query( "PRODUCTS", new String[] {"DESCRIPTION", "PRICE", "PROMOTION"},  "PROMOTION = ?",
                    new String[] {"oferta"},  null, null, "DESCRIPTION ASC" );
            String text = "Promociones: \n";

            if(cursorProductPromotionsGet.moveToFirst()){
                int sizeOfCursor = cursorProductPromotionsGet.getCount();

                for(int i=0 ; i < sizeOfCursor ; i++){
                    cursorProductPromotionsGet.moveToPosition(i);
                    text += cursorProductPromotionsGet.getString(0) + " \n --- $ " + cursorProductPromotionsGet.getString(1) + " \n ::: \n ";
                }

                Toast.makeText(this, text , Toast.LENGTH_LONG).show();

                Intent whatsappIntent = new Intent();

                whatsappIntent.setAction(Intent.ACTION_SEND);

                //PackageInfo packInfo = pm.getPackageInfo("com.whatsapp" , PackageManager.GET_META_DATA);  //if it fails,  catch will see it

                whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");

                startActivity(whatsappIntent);                // not sure if useful?? --> //            startActivity(Intent.createChooser(whatsappIntent, "Share with"));
            }else{
                text = "No se encuentra Promociones... \n \n  porfavor seleciona los productos en 'Oferta'";

                Toast.makeText(this, text , Toast.LENGTH_LONG).show();
            }
            cursorProductPromotionsGet.close();
        }
        else {
            Cursor cursorProductPromotionsGet = db.query("PRODUCTS", new String[]{"DESCRIPTION", "PRICE", "PROMOTION"}, "PROMOTION = ?",
                    new String[]{"oferta"}, null, null, "DESCRIPTION ASC");
            String text = "Promociones: \n";

            if (cursorProductPromotionsGet.moveToFirst()) {
                int sizeOfCursor = cursorProductPromotionsGet.getCount();

                for (int i = 0; i < sizeOfCursor; i++) {
                    cursorProductPromotionsGet.moveToPosition(i);
                    text += cursorProductPromotionsGet.getString(0) + "\n --- $ " + cursorProductPromotionsGet.getString(1) + " \n ::: \n ";
                }

                Toast.makeText(this, "WhatsAPP esta instalado?" + " --- \n \n" + text, Toast.LENGTH_LONG).show();
            }else{
            text = "No se encuentra Promociones... \n \n porfavor seleciona los productos en 'Oferta'";

            Toast.makeText(this,  "WhatsAPP esta instalado?" + " --- \n \n" + text , Toast.LENGTH_LONG).show();
            }
            cursorProductPromotionsGet.close();
        }

    }

    public void initiatePopupProductsWindow(){

        LayoutInflater inflater = (LayoutInflater) products.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_product_info_input_group);

        View layout = inflater.inflate(R.layout.popup_product_info_input, popupGroup);


        popupProductWindow = new PopupWindow(layout, screenWidth, screenHeight + 80, true);

        popupProductWindow.showAtLocation(layout, Gravity.START, 0, 0);

        popupProductBarcode             = (EditText) layout.findViewById(R.id.popup_product_info_barcode);
        popupProductDescription         = (EditText) layout.findViewById(R.id.popup_product_info_description);
        popupProductPrice               = (EditText) layout.findViewById(R.id.popup_product_info_price);
        popupProductPromotionCheckbox   = (CheckBox) layout.findViewById(R.id.popup_product_info_promotion_checkbox);
        popupProductBarcodeTextTitle    = (TextView)    layout.findViewById(R.id.popup_product_info_textview1);
        popupProductImage               = (ImageView) layout.findViewById(R.id.popup_product_info_imageview);
        popupProductImageAddress        = (EditText) layout.findViewById(R.id.popup_product_info_photo_address);
        popupProductButtonGetPhoto      = (Button) layout.findViewById(R.id.popup_product_info_button_photo);
        popupProductButtonSendToWhatsApp = (Button) layout.findViewById(R.id.popup_product_info_button_whatsapp);

        popupProductBarcode.setText(BARCODEstring);
        popupProductDescription.setText(DESCRIPTIONstring);
        popupProductPrice.setText(PRICEstring);

            popupProductBarcode.setEnabled(false);
        String quickBarcode = popupProductBarcode.getEditableText().toString();
        String firstLetter;
        if ("".equals(quickBarcode) | quickBarcode == null ) { firstLetter = ""; }
        else{ firstLetter = quickBarcode.substring(0,1); }

        imageUriInfo = null;
        Bitmap productPic = null;

        popupProductImage.setImageBitmap(null);
        popupProductImage.setMinimumWidth(screenWidth - 120);
        //int imageHeight = (int) (screenHeight / screenWidth ) * 100 ;
        popupProductImage.setMinimumHeight(screenWidth - 120);
        //popupProductImage.setMaxHeight(screenHeight - 120);
       // popupProductImage.refreshDrawableState();

        popupProductImageAddress.setText("");

        //if("pn_".equals(firstLetter))
        {
            popupProductImage.setEnabled(true);
            popupProductImageAddress.setEnabled(false);
            popupProductButtonGetPhoto.setEnabled(true);
            popupProductButtonSendToWhatsApp.setEnabled(true);
            popupProductButtonSendToWhatsApp.setBackgroundColor(0xFF63E73F);

            //productPic = (Bitmap) readImageFromInternalStorage(quickBarcode);
            imageUriInfo = null;
            productPic = null;

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE} , MY_PERMISSIONS_REQUEST_READ_ACCESS);
            }
            else {
                productPic = (Bitmap) readFullImageFromExternalStorage(quickBarcode);
            }

            if (productPic != null) {
                popupProductImage.setScaleType(ImageView.ScaleType.FIT_XY);
                //popupProductImage.setCropToPadding(true);                             // api 16 REQUIRED
                    //popupProductImage.setImageBitmap(productPic);
                    //imageUriInfo = Uri.fromFile(new File(getFilesDir() , filename ) );
                    //imageUriInfo = getLocalBitmapUri(popupProductImage) ;                             <--- DOH!!!!  that's a custom method, not found in android, but built from it!!!!!!
                    //String filename = quickBarcode + ".jpg";
                    // int picwidth = productPic.getWidth();
                    // int picheight = productPic.getHeight();

                    // int wantedHeight = (int) (picheight / picwidth) * (screenWidth - 120);
                    // wantedHeight = (int) wantedHeight;

                    //if(android.os.Build.VERSION.SDK_INT > 15) {

                    // popupProductImage.setCropToPadding(true);
                    //}

                    //popupProductImage.setMaxHeight( screenWidth - 120 );

                popupProductImage.setAdjustViewBounds(true);

                popupProductImage.setImageURI(imageUriInfo);
                popupProductImageAddress.setText(imageUriInfo.getLastPathSegment().toString());
                popupProductButtonGetPhoto.setText("Reemplazar Foto?");
            } else {
                popupProductImage.setImageBitmap(null);
                popupProductImageAddress.setText("");
                popupProductButtonGetPhoto.setText("Tomar una nueva Foto?");
            }


        }
        /*
        else{
            popupProductImage.setEnabled(false);
            popupProductImageAddress.setEnabled(false);
            popupProductButtonGetPhoto.setEnabled(false);
            popupProductButtonSendToWhatsApp.setEnabled(false);
            popupProductButtonSendToWhatsApp.setBackgroundColor(0xFFFFFFFF);
        }
        */

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
                miWarehouseDbHelper.updateColumn(db, TABLENAME, BARCODEstring, "DESCRIPTION" , DESCRIPTIONstring,
                        "PRICE" , PRICEstring, "CATEGORY", CATEGORYstring, "PROMOTION", PROMOTIONstring);
            }
            else {
                miWarehouseDbHelper.insertProductsRecord(db, TABLENAME, BARCODEstring, DESCRIPTIONstring, "0",
                        PRICEstring, CATEGORYstring, PROMOTIONstring);
            }
            popupProductWindow.dismiss();
            getProductsDatatableToList(TABLENAME);
            cursorBarcodeChk.close();
        }
    }

    public void cancelPopupProductsWindow(View v){
        popupProductWindow.dismiss();
    }

    public void initiatePopupProductCustomInput(View v){
                                            /////////////////////////////////////////   <---- was here,  always introduced incorrect picture

        String quickQuery = "SELECT BARCODE FROM PRODUCTS WHERE BARCODE like \'pn_%\' ORDER BY length(BARCODE) DESC , BARCODE DESC";
        Cursor cursorTableSizing = db.rawQuery(quickQuery, null);
        String quickCode;

         if(cursorTableSizing.moveToFirst()){
                cursorTableSizing.moveToFirst();
                quickCode = cursorTableSizing.getString(0);
                quickCode = quickCode.replace("pn_" , "");
                }
         else{   quickCode = "0"; }

        int sizer = Integer.parseInt(quickCode);
            sizer++;
        //Toast toast = Toast.makeText(this, quickCode + " --- " + sizer, Toast.LENGTH_LONG);
        //toast.show();

        BARCODEstring = "pn_"+ Integer.toString(sizer);

        initiatePopupProductsWindow();      /////////////////////////////////////////         <--------  Ordering influence is important.
        popupProductImage.setImageBitmap(null);
        popupProductImageAddress.setText("");
        popupProductButtonGetPhoto.setText("Tomar una nueva Foto?");
        popupProductBarcode.setText(BARCODEstring);
        popupProductDescription.setText("");
        popupProductPrice.setText("0");
        popupProductPromotionCheckbox.setChecked(false);
        /*
        popupProductImage.setEnabled(true);
        popupProductImageAddress.setEnabled(true);
        popupProductButtonGetPhoto.setEnabled(true);
        */
    }

    public void initiatePopupProductDeleteRecordWindow(View v){
        try{
            LayoutInflater inflater = (LayoutInflater) products.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup popupGroup = (ViewGroup) findViewById(R.id.popup_window_two);

            View layout = inflater.inflate(R.layout.popup_two_layout, popupGroup);

            popupProductDeleteRecordWindow = new PopupWindow(layout, screenWidth, screenHeight, true);
            popupProductWindow.dismiss();
            popupProductDeleteRecordWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closePopupWindowTwo(View v){
        miWarehouseDbHelper.deleteRecord(db, TABLENAME, BARCODEstring);
        popupProductDeleteRecordWindow.dismiss();
        getProductsDatatableToList(TABLENAME);
    }

    static final int MY_PERMISSIONS_REQUEST_CAMERA_ACCESS = 1;
    static final int MY_PERMISSIONS_REQUEST_WRITE_ACCESS = 2;
    static final int MY_PERMISSIONS_REQUEST_READ_ACCESS = 3;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[] , int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_CAMERA_ACCESS : {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //carry on with accessing Camera
                    snapPicture(popupProductButtonGetPhoto);
                } else{
                    // attempt something without the use of the CAMERA
                    Toast.makeText(this, "No habilitaste la Camera, revisa porfavor" , Toast.LENGTH_LONG).show();
                }

                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_ACCESS : {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //carry on with writing tasks.
                    snapPicture(popupProductButtonGetPhoto);
                }else {
                    Toast.makeText(this, "No habilitaste accesso al los Archivos, revisa porfavor" , Toast.LENGTH_LONG).show();
                }

            }

            case MY_PERMISSIONS_REQUEST_READ_ACCESS : {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //carry on with writing tasks.

                }else {
                    Toast.makeText(this, "No habilitaste accesso al los Archivos, revisa porfavor" , Toast.LENGTH_LONG).show();
                }

            }
        }
    }



    public void snapPicture(View v){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA} , MY_PERMISSIONS_REQUEST_CAMERA_ACCESS);
        }
        else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , MY_PERMISSIONS_REQUEST_WRITE_ACCESS);
            }
            else {

                Intent takeFullPictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                String filename = popupProductBarcode.getEditableText().toString();

                File imageFile = createImageFileInExternalStorage(filename);
                if (imageFile != null) {
                    imageUriInfo = Uri.fromFile(imageFile);
                    takeFullPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriInfo);
                    startActivityForResult(takeFullPictureIntent, REQUESTCODE_FULL_PICTURE);
                } else {
                    Toast.makeText(this, "El archivo no existe", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void oldSnapPicture(View v){
         Intent snapPictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         startActivityForResult(snapPictureIntent, REQUESTCODE_THUMBNAIL_FROM_CAMERA);
    }



    public void WorkingVersionOf_snapPicture(View v){

        Intent takeFullPictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        String filename = popupProductBarcode.getEditableText().toString();

        File imageFile = createImageFileInExternalStorage(filename);
        if(imageFile != null) {
            imageUriInfo = Uri.fromFile(imageFile);
            takeFullPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriInfo);
            startActivityForResult(takeFullPictureIntent, REQUESTCODE_FULL_PICTURE);
        }else{
            Toast.makeText(this, "El archivo no existe" , Toast.LENGTH_LONG).show();
        }

    }

    public boolean saveImageToInternalStorage(Bitmap image, String filename){
        try {
            filename = filename + ".jpg";
            FileOutputStream fos = openFileOutput( filename, Context.MODE_PRIVATE);

            image.compress(Bitmap.CompressFormat.JPEG, 100 , fos);

            fos.close();

            return true;
        } catch (Exception e) {
            Log.e("svToInternalStorage() ", e.getMessage());
            return false;
        }

    }

    public final static String APP_PATH_SDCARD_PICTURES = "/miBodega_basico/";
    public final static String APP_PATH_PRODUCTS = "productos";

    public boolean saveImageToExternalStorage(Bitmap image, String filename){
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS;
        try{
            /*File dir1 = new File( "/storage/sdcard0/myBodega_basico" );
            if (!dir1.exists())              {        dir1.mkdirs();}
            File dir = new File( "/storage/sdcard0/myBodega_basico/productos" );
            if (!dir.exists())              {        dir.mkdirs();}
            */
                    //////////////////////////////////////////////////////////////////////////////// Can't mkdirs()  of two directorys in one go, So make them one by one.

            File dir1 = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES );
            if (!dir1.exists())              {        dir1.mkdirs();}
            File dir = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS );
            if (!dir.exists())              {        dir.mkdirs();}


            OutputStream fOut = null;
            String fullFilename = filename + ".png";

            //File file = new File( "/storage/sdcard0/myBodega_basico/productos/" + fullFilename);
            File file = new File(fullPath + "/" + fullFilename);

            file.createNewFile();
            fOut = new FileOutputStream(file);

            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            //////  Add this saved Image to the  Common MediaStore????  NOT Yet

            return true;
        }catch (Exception e){
            Log.e("svToInternalStorage()" , e.getMessage());
            return false;
        }

    }

    public File createImageFileInExternalStorage(String filename){
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS;

        //String firstPath19 = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() +  APP_PATH_SDCARD_PICTURES;
        //String secondPath19 = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS;
        try{
            File dir1 = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES );
            if (!dir1.exists())              {        dir1.mkdirs();}
            File dir = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS );
            if (!dir.exists())              {        dir.mkdirs();}

            String fullFilename = filename + ".jpg";
            File file = new File(fullPath , fullFilename);
            file.createNewFile();

            //File dir19 = getExternalFilesDir(Environment.DIRECTORY_DCIM);
            //if (!dir19.exists())              {        dir19.mkdirs();}
            //File dir019 = new File( secondPath19);
            //if (!dir019.exists())              {        dir019.mkdirs();}
            //OutputStream fOut = null;
            //File file = File.createTempFile(filename, ".jpg" , dir19);

            return file;
        }catch (Exception e){
            Log.e("svToInternalStorage()" , e.getMessage());
            return null;
        }

    }

    public Bitmap readImageFromInternalStorage(String filename){
        Bitmap imageFile = null;
        try{
            filename = filename + ".jpg";

            File filePath = getFileStreamPath(filename);
            FileInputStream fis = new FileInputStream(filePath);
            imageFile = BitmapFactory.decodeStream(fis);
            imageUriInfo = Uri.fromFile(filePath);

        }catch (Exception ex){
            Log.e("readingFromStor()", ex.getMessage());
        }
        return imageFile;
    }

    public Bitmap readImageFromExternalStorage(String filename){
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS;
        //  fullPath does not work for some reason????!!!

                Bitmap imageFile = null;
        try{
            String fullFilename = filename + ".png";
            // imageFile = BitmapFactory.decodeFile("/storage/sdcard0/myBodega_basico/productos/" + fullFilename);
            // imageUriInfo = Uri.fromFile(new File("/storage/sdcard0/myBodega_basico/productos/" + fullFilename ) );

            imageFile = BitmapFactory.decodeFile(fullPath + "/" + fullFilename);

            imageUriInfo = Uri.fromFile(new File(fullPath + "/" + fullFilename ) );

        }catch (Exception ex) {
            Log.e("readingFromExStor" , ex.getMessage());
        }

        return imageFile;
    }

    public Bitmap readFullImageFromExternalStorage(String filename){
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_PATH_SDCARD_PICTURES + APP_PATH_PRODUCTS;
        //  fullPath does not work for some reason????!!!
        //String dir19 = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath();

        Bitmap imageFile = null;
        try{
            String fullFilename = filename + ".jpg";
            // imageFile = BitmapFactory.decodeFile("/storage/sdcard0/myBodega_basico/productos/" + fullFilename);
            // imageUriInfo = Uri.fromFile(new File("/storage/sdcard0/myBodega_basico/productos/" + fullFilename ) );

            imageFile = BitmapFactory.decodeFile(fullPath + "/" + fullFilename);

            imageUriInfo = Uri.fromFile(new File(fullPath + "/" + fullFilename ) );


            //  INVESTIGATE how to use FILE PERMISSIONS for SDK 23 and greater
            //imageFile = BitmapFactory.decodeFile(dir19 + "/" + fullFilename);
            //imageUriInfo = Uri.fromFile(new File(dir19 + "/" + fullFilename ) );

        }catch (Exception ex) {
            Log.e("readingFromExStor" , ex.getMessage());
        }

        return imageFile;
    }

    public void checkPictureAddressBeforeWhatsApp(View v){
        boolean readyToSend = true;
        String text = "Falta Informacion: \n";
        String quickImageAddress        = popupProductImageAddress.getEditableText().toString();
        String quickBarcode             = popupProductBarcode.getEditableText().toString();
        String quickDescription         = popupProductDescription.getEditableText().toString();
        String quickPrice               = popupProductPrice.getEditableText().toString();

                if ("".equals(quickDescription) | quickDescription == null) {
                    text = text + "   falta una Descipcion del Producto \n";
                    readyToSend = false;
                }
                if ("".equals(quickPrice) | quickPrice == null | "0".equals(quickPrice)) {
                    text = text + "   falta un Precio del Producto \n";
                    readyToSend = false;
                }
                if ("".equals(quickImageAddress) | quickImageAddress == null) {
                    text = text + "   Tiene Foto? \n";
                    readyToSend = false;
                }

        if(readyToSend == false)    {           Toast.makeText(this, text, Toast.LENGTH_LONG).show();       }
        else                        {           sendPicWithCaptionToWhatsApp(v);                            }
    }

    public void sendPicWithCaptionToWhatsApp(View v) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        /////////////////////////////////////   DOEsn't WORK in  BB OS.10  with  Android Emulation.....
        /////  cheat code to force step-into
        ////////////////////////////////////    app_installed = true;   // <-- remove once done...

        if(app_installed == true) {

            String text = "";

            String quickBarcode         = popupProductBarcode.getEditableText().toString();
            String quickDescription     = popupProductDescription.getEditableText().toString();
            String quickPrice           = popupProductPrice.getEditableText().toString();
            String quickImageUri        = popupProductImageAddress.getEditableText().toString();

            text = quickDescription + "\n" + "Precio : $ " + quickPrice ;

            Toast.makeText(this, text , Toast.LENGTH_LONG).show();

            Intent whatsappIntent = new Intent(android.content.Intent.ACTION_SEND);

            //whatsappIntent.setAction(Intent.ACTION_SEND);

            //PackageInfo packInfo = pm.getPackageInfo("com.whatsapp" , PackageManager.GET_META_DATA);  //if it fails,  catch will see it
            //grantUriPermission("com.whatsapp", imageUriInfo, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);
            whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUriInfo);
            whatsappIntent.setType("image/*");

            whatsappIntent.setPackage("com.whatsapp");

            //  whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) ;
            //  startActivity(whatsappIntent);                // not sure if useful?? --> //            startActivity(Intent.createChooser(whatsappIntent, "Share with"));
            //  startActivityForResult(Intent.createChooser(whatsappIntent, "share Image"), REQUESTCODE_FROM_WHATSAPP);
            //  startAcitvityForResult()  <---  used since newer programs find it more reliable than  ...  startActivity();

            startActivityForResult(whatsappIntent, REQUESTCODE_FROM_WHATSAPP);
        }
        else{
            String text = "";

            String quickBarcode         = popupProductBarcode.getEditableText().toString();
            String quickDescription     = popupProductDescription.getEditableText().toString();
            String quickPrice           = popupProductPrice.getEditableText().toString();
            String quickImageUri        = popupProductImageAddress.getEditableText().toString();

            text = quickBarcode + "\n" + quickDescription + "\n" + "Precio : $ " + quickPrice + "\n" + quickImageUri ;

            Toast.makeText(this, "WhatsAPP no esta Instalado" + " --- \n \n " + text , Toast.LENGTH_LONG).show();
        }
    }
}
