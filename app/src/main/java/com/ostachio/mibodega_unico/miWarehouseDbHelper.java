package com.ostachio.mibodega_unico;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;


public class miWarehouseDbHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "miWarehouseDb";
    private static final int    DB_VERSION = 6;

    miWarehouseDbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db){
        createTable(db, "STORAGE" );
        createTable(db, "SHELVES" );
        createTable(db, "CASHIER" );
        createTable(db, "PRODUCTS" );
        createTableCASHIERHISTORY(db, "CASHIER_HISTORY");
        createTableTransactions(db, "HISTORYSALES");
    }
                 private void createTable(SQLiteDatabase db, String tableName ){
                    String tableProducts = ", TIME_STAMPING TIMESTAMP default current_timestamp ";

                    if( tableName == "PRODUCTS" ){
                        tableProducts = tableProducts + ", CATEGORY TEXT , PROMOTION TEXT ";
                    }

                    db.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "BARCODE TEXT, "
                            + "DESCRIPTION TEXT, "
                            + "QUANTITY INTEGER, "
                            + "PRICE INTEGER "
                            + tableProducts + " );"
                    );
                }

                private void createTableTransactions(SQLiteDatabase db, String tableName ){

                    db.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "TIME_STAMPING TIMESTAMP default current_timestamp, "
                            + "SOLDQUANTITY INTEGER, "
                            + "TOTALPRICE INTEGER, "
                            + "CUSTOMERPAYS INTEGER, "
                            + "CUSTOMERRECEIPT INTEGER "
                            + " );"
                    );
                }

                private void createTableCASHIERHISTORY(SQLiteDatabase db, String tableName ){

                    db.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "BARCODE TEXT, "
                            + "DESCRIPTION TEXT, "
                            + "QUANTITY INTEGER, "
                            + "PRICE_COL_LEFT INTEGER, "
                            + "PRICE_COL_RIGHT INTEGER, "
                            + "TIME_STAMPING TIMESTAMP default current_timestamp "
                            + " );"
                    );
                }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
         if (oldVersion < 2) {
             upgradeTableVerONEtoTWO(db, "STORAGE" );
             upgradeTableVerONEtoTWO(db, "SHELVES" );
             upgradeTableVerONEtoTWO(db, "CASHIER" );
          }

        if (oldVersion < 3) {
            upgradeTableSalesVerTWOtoTHREE(db, "HISTORYSALES");
            createTable(db, "HISTORYCASHIER");
        }

        if(oldVersion < 4){
            upgradeTableProductsVerTHREEtoFOUR(db, "PRODUCTS");
        }
        if(oldVersion <5) {
            upgradeTableSalesVerFOURtoFIVE(db, "HISTORYSALES");
        }
        if(oldVersion <6) {
            upgradeTableSalesVerFIVEtoSIX(db, "HISTORYSALES");
        }
    }
               private void upgradeTableVerONEtoTWO(SQLiteDatabase db, String tableName){
                    // code when needing to UPGRADE the old tables.
                    // Gives  "Cannot add col with non-constant default" <-- means that old table,
                    //   missing a VIP col  due to 'default' setting
                    //// have to go the long way to fix it.
                    // db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN TIME_STAMPING TIMESTAMP default current_timestamp ;");   // ---> p 468

                    db.execSQL(" ALTER TABLE " + tableName + " RENAME TO tempName ;");
                    createTable(db, tableName);
                    db.execSQL("INSERT INTO " + tableName + " (BARCODE , DESCRIPTION, QUANTITY, PRICE) "
                            + "SELECT BARCODE, DESCRIPTION, QUANTITY, PRICE FROM tempName ;"       );
                    db.execSQL(" DROP TABLE tempName ; ");
                }

                private void upgradeTableSalesVerTWOtoTHREE(SQLiteDatabase db, String tableName){

                    db.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "TIME_STAMPING TIMESTAMP default current_timestamp, "
                            + "SOLDQUANTITY INTEGER, "
                            + "TOTALPRICE INTEGER, "
                            + "CUSTOMERPAYS INTEGER, "
                            + "CUSTOMERRECEIPT INTEGER "
                            + " );"
                    );
                }

                private void upgradeTableSalesVerFOURtoFIVE(SQLiteDatabase db, String tableName){
                    db.execSQL("ALTER TABLE " + tableName + " RENAME TO tempHistorySales; ");
                    createTableTransactions(db, tableName);
                    db.execSQL("INSERT INTO " + tableName + " ( SOLDQUANTITY , TOTALPRICE, CUSTOMERPAYS , CUSTOMERRECEIPT ) "
                    + "SELECT QUANTITY, TOTALPRICE, CUSTOMERPAYS, CUSTOMERRECEIPT FROM tempHistorySales ;");
                }
            {              /*    private void updateTable(SQLiteDatabase db, String tableName ){
                                                        String tableProducts = "";

                                                        if( tableName == "PRODUCTS"){
                                                            tableProducts = ", CATEGORY TEXT ";
                                                        }

                                                        db.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                                + "BARCODE TEXT, "
                                                                + "DESCRIPTION TEXT, "
                                                                + "QUANTITY INTEGER, "
                                                                + "PRICE INTEGER "
                                                                + tableProducts + " );"
                                                        );
                                                    }
                                                */  }

                private void upgradeTableProductsVerTHREEtoFOUR(SQLiteDatabase db, String tableName){
                   db.execSQL("DROP TABLE PRODUCTS;");
                   createTable(db, "PRODUCTS");

                    // Adding Coloumn with TIMESTAMP default ---- won't work, since a table is already in existence, but can't
                    // add another col specifically NON-EMPTY,  what about the previous rows that already have nothing in them?
                    // the database doesn't know what to do about the old rows,  since you're specifying that ALL rows can't be empty
                    // db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN TIME_STAMPING TIMESTAMP default current_timestamp ;");
                   // db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN PROMOTION TEXT DEFAULT 0 ;");

                }

                            private void upgradeTableSalesVerFIVEtoSIX(SQLiteDatabase db, String tableName) {
                                db.execSQL("DROP TABLE HISTORYCASHIER;");
                                db.execSQL("DROP TABLE HISTORYSALES;");
                                createTable(db, "CASHIER_HISTORY");
                            }

    public static void insertRecord(SQLiteDatabase db, String tableName, String barcode,  String description,  int quantity, int price){

    db.execSQL("INSERT INTO " + tableName + " ( BARCODE , DESCRIPTION , QUANTITY , PRICE ) "
                + " VALUES ( \'" + barcode + "\' , \'" + description + "\' , " + quantity + " , " + price  +  " ); " );
                                        {         /*        ContentValues productRecord = new ContentValues();

                                                                    productRecord.put("BARCODE", barcode);
                                                                    productRecord.put("DESCRIPTION", description);
                                                                    productRecord.put("QUANTITY", quantity);
                                                                    productRecord.put("PRICE", price);

                                                                    db.insert(tableName, null, productRecord);
                                                            */  }
    }

    public static void insertRecord(SQLiteDatabase db, String tableName, String barcode,  String description,  int quantity, int price, String category){

    db.execSQL("INSERT INTO " + tableName + " ( BARCODE , DESCRIPTION , QUANTITY , PRICE ) "
            + " VALUES ( \'" + barcode + "\' , \'" + description + "\' , " + quantity + " , " + price  + " , " + category +  " ); " );
                                            /* Scrap this method
                                                ContentValues productRecord = new ContentValues();
                                                    productRecord.put("BARCODE", barcode);         productRecord.put("DESCRIPTION", description);        productRecord.put("QUANTITY", quantity);
                                                    productRecord.put("PRICE", price);        productRecord.put("CATEGORY", category);
                                                  // --->  Don't know how to use it yet..............  productRecord.put("TIME_STAMPING", getDateTime() );
                                                    db.insert(tableName, null, productRecord);
                                            */
    }

    public static void insertRecordAccount(SQLiteDatabase db, String tableName, String barcode,  String description,  int quantity, int priceLeft, int priceRight){

        db.execSQL("INSERT INTO " + tableName + " ( BARCODE , DESCRIPTION , QUANTITY , PRICE_COL_LEFT , PRICE_COL_RIGHT ) "
                + " VALUES ( \'" + barcode + "\' , \'" + description + "\' , " + quantity + " , " + priceLeft  + " , " + priceRight +  " ); " );
    }

    public static void insertSalesRecord(SQLiteDatabase db, String tableName, int soldquantity, int totalprice , int customerpays , int customerreceipt ){
        ContentValues salesRecord = new ContentValues();

        salesRecord.put("SOLDQUANTITY", soldquantity);
        salesRecord.put("TOTALPRICE", totalprice);
        salesRecord.put("CUSTOMERPAYS", customerpays);
        salesRecord.put("CUSTOMERRECEIPT", customerreceipt);

        db.insert(tableName, null, salesRecord);
    }

    public static void insertTableIntoHistoryCashier(SQLiteDatabase db, String oldTableName, String newTableName ) {
        db.execSQL("INSERT INTO " + newTableName + " ( BARCODE, DESCRIPTION, QUANTITY, PRICE ) "
                + "SELECT BARCODE, DESCRIPTION, QUANTITY, PRICE FROM " + oldTableName + " ;" );
    }

    public static void insertTableIntoCASHIERHISTORY(SQLiteDatabase db, String oldTableName, String newTableName ) {
        db.execSQL("INSERT INTO " + newTableName + " ( BARCODE, DESCRIPTION, QUANTITY, PRICE_COL_RIGHT ) "
                + "SELECT BARCODE, DESCRIPTION, QUANTITY, PRICE as PRICE_COL_RIGHT FROM " + oldTableName + " ;" );
        db.execSQL("DELETE FROM " + oldTableName + " ;");
        db.execSQL("VACUUM;");
    }

    public static void insertProductsRecord(SQLiteDatabase db, String tableName, String barcode, String description,
                                            String quantity, String price, String category, String promotion ) {
       // db.execSQL("INSERT INTO " + tableName + " ( BARCODE, DESCRIPTION, QUANTITY, PRICE, CATEGORY, PROMOTION ) "
       //         + " VALUES  ;" );

        ContentValues productsRecord = new ContentValues();

        productsRecord.put("BARCODE", barcode);
        productsRecord.put("DESCRIPTION", description );
        productsRecord.put("QUANTITY", quantity );
        productsRecord.put("PRICE", price );
        productsRecord.put("CATEGORY", category );
        productsRecord.put("PROMOTION", promotion);

        db.insert(tableName, null, productsRecord);
    }

                                                    {     /*
                                                            public static void updateRecordQuantity(SQLiteDatabase db, String tableName, String barcode, int quantity){
                                                                ContentValues productRecord = new ContentValues();

                                                                productRecord.put("QUANTITY", quantity);

                                                                db.update(tableName, productRecord, "BARCODE = ?", new String[] {barcode});
                                                            }

                                                            public static void updateRecordPrice(SQLiteDatabase db, String tableName, String barcode, int price){
                                                                ContentValues productRecord = new ContentValues();

                                                                productRecord.put("PRICE", price);

                                                                db.update(tableName, productRecord, "BARCODE = ?", new String[] {barcode});
                                                            }
                                                        */ }

    public static void deleteRecord(SQLiteDatabase db, String tableName, String barcode){
        db.delete(tableName, "BARCODE = ?" , new String[] {barcode} );
    }


    public static void updateColumn(SQLiteDatabase db, String tableName, String barcode,
                                    String columnName , String columnValue ){
        db.execSQL("UPDATE " + tableName + " SET "
                + columnName + " = \'" + columnValue + "\' , TIME_STAMPING = datetime('now') "
                + " WHERE BARCODE = \'" + barcode + "\' ;" );
    }

                                    public static void updateColumn(SQLiteDatabase db, String tableName, String barcode,
                                                                    String columnName1 , String columnValue1 ,
                                                                    String columnName2 , String columnValue2 ){
                                        db.execSQL("UPDATE " + tableName + " SET "
                                                + columnName1 + " = \'" + columnValue1 + "\' , "
                                                + columnName2 + " = \'" + columnValue2 + "\' , TIME_STAMPING = datetime('now') "
                                                + " WHERE  BARCODE = \'" + barcode + "\' ;" );
                                    }

                                                            public static void updateColumn(SQLiteDatabase db, String tableName, String barcode,
                                                                                            String columnName1 , String columnValue1 ,
                                                                                            String columnName2 , String columnValue2 ,
                                                                                            String columnName3 , String columnValue3 ){
                                                                db.execSQL("UPDATE " + tableName + " SET "
                                                                        + columnName1 + " = \'" + columnValue1 + "\' , "
                                                                        + columnName2 + " = \'" + columnValue2 + "\' , "
                                                                        + columnName3 + " = \'" + columnValue3 + "\' , TIME_STAMPING = datetime('now') "
                                                                        + " WHERE BARCODE = \'" + barcode + "\' ;" );
                                                            }

                                            public static void updateColumn(SQLiteDatabase db, String tableName, String barcode,
                                                                            String columnName1 , String columnValue1 ,
                                                                            String columnName2 , String columnValue2 ,
                                                                            String columnName3 , String columnValue3,
                                                                            String columnName4 , String columnValue4 ){
                                                db.execSQL("UPDATE " + tableName + " SET "
                                                        + columnName1 + " = \'" + columnValue1 + "\' , "
                                                        + columnName2 + " = \'" + columnValue2 + "\' , "
                                                        + columnName3 + " = \'" + columnValue3 + "\' , "
                                                        + columnName4 + " = \'" + columnValue4 + "\' , TIME_STAMPING = datetime('now') "
                                                        + " WHERE BARCODE = \'" + barcode + "\' ;" );
                                            }

                            public static void updateColumn(SQLiteDatabase db, String tableName, String barcode,
                                                            String columnName1 , String columnValue1 ,
                                                            String columnName2 , String columnValue2 ,
                                                            String columnName3 , String columnValue3 ,
                                                            String columnName4 , String columnValue4 ,
                                                            String columnName5 , String columnValue5 ){
                                db.execSQL("UPDATE " + tableName + " SET "
                                        + columnName1 + " = \'" + columnValue1 + "\' , "
                                        + columnName2 + " = \'" + columnValue2 + "\' , "
                                        + columnName3 + " = \'" + columnValue3 + "\' , "
                                        + columnName4 + " = \'" + columnValue4 + "\' , "
                                        + columnName5 + " = \'" + columnValue5 + "\' , TIME_STAMPING = datetime('now') "
                                        + " WHERE BARCODE = \'" + barcode + "\' ;" );
                            }
}
