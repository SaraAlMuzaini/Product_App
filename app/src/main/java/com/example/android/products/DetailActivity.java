package com.example.android.products;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.products.data.ProductContract.ProductEntry;

import java.io.ByteArrayInputStream;

import static android.R.id.message;

/**
 * Allows user to show information about an existing product.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 2;

    /**
     * Content URI for the existing product
     */
    private Uri mCurrentProductUri;

    /**
     * TextView field to show the product's name
     */
    private TextView mNameTextView;


    /**
     * TextView field to show the product's quantity
     */
    private TextView mQuantityTextView;
    private int mCurrentQuantity;
    /**
     * TextView field to show the product's price
     */
    private TextView mPriceTextView;

    private String supplierEmail;

    private TextView mSupplierNameTextView;

    private ImageView mPictureProduct;
    //Buttons
    private ImageButton mSupplierEmailImageButton;
    private ImageButton mIncrementImageButton;
    private ImageButton mDecrementImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        // Examine the intent that was used to launch this activity,
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        setContentView(R.layout.detail_layout);

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            finish();
        }

        // Find all relevant views that we will need to print info on it
        mNameTextView = (TextView) findViewById(R.id.text_name);
        mQuantityTextView = (TextView) findViewById(R.id.text_quantity);
        mPriceTextView = (TextView) findViewById(R.id.text_price);
        mSupplierNameTextView = (TextView) findViewById(R.id.text_supplier_name);
        mPictureProduct = (ImageView) findViewById(R.id.image_detail);
        //button
        mIncrementImageButton = (ImageButton) findViewById(R.id.increment_button);
        mDecrementImageButton = (ImageButton) findViewById(R.id.decrement_button);
        mSupplierEmailImageButton = (ImageButton) findViewById(R.id.supplier_email);

        // On click for ordering from supplier (send email to supplier)
        mSupplierEmailImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        //increment quantity
        mIncrementImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update quantity +1
                ContentValues updateValues = new ContentValues();

                updateValues.put(ProductEntry.COLUMN_QUANTITY, mCurrentQuantity + 1);

                int rowsAffected = getContentResolver().update(mCurrentProductUri, updateValues, null, null);

                if (rowsAffected > 0) {
                    Toast.makeText(getApplicationContext(), "Quantity has been increase", Toast.LENGTH_SHORT).show();
                    getContentResolver().notifyChange(mCurrentProductUri, null);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not update quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //decrement quantity
        mDecrementImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update quantity -1
                ContentValues updateValues = new ContentValues();

                if (mCurrentQuantity - 1 < 0) {
                    Toast.makeText(getApplicationContext(), "Insufficient quantity to complete order", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateValues.put(ProductEntry.COLUMN_QUANTITY, mCurrentQuantity - 1);

                int rowsAffected = getContentResolver().update(mCurrentProductUri, updateValues, null, null);

                if (rowsAffected > 0) {
                    Toast.makeText(getApplicationContext(), "Quantity has been decrease", Toast.LENGTH_SHORT).show();
                    getContentResolver().notifyChange(mCurrentProductUri, null);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not update quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void sendEmail() {
        String subject = getString(R.string.send_email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", supplierEmail, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(intent, "Choose an Email client :"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the detail shows all product attributes, define a projection that contains
        // all columns from the products table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in

            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            supplierEmail = cursor.getString(supplierEmailColumnIndex);
            mCurrentQuantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            byte[] photo = cursor.getBlob(pictureColumnIndex);


            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mSupplierNameTextView.setText(supplierName);
            mQuantityTextView.setText(Integer.toString(mCurrentQuantity));
            mPriceTextView.setText(Integer.toString(price));
            if (photo == null) {
                mPictureProduct.setImageResource(R.mipmap.ic_launcher);
            } else {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(photo);
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                mPictureProduct.setImageBitmap(imageBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameTextView.setText("");
        mSupplierNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
    }


    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}