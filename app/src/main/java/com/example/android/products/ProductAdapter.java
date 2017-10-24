/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.products;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.products.data.ProductContract.ProductEntry;


/**
 * {@link ProductAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.text_name_list);
        TextView mQuantityTextView = (TextView) view.findViewById(R.id.text_quantity_list);
        TextView mPriceTextView = (TextView) view.findViewById(R.id.text_price_list);
        ImageButton mSaleButton = (ImageButton) view.findViewById(R.id.sale_product_list);
        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);

        int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        final Uri mCurrentProductUri = Uri.parse(ProductEntry.CONTENT_URI + "/" + id);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        final int mCurrentQuantity = cursor.getInt(quantityColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        //sale product decrement quantity
        mSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update quantity -1
                ContentValues updateValues = new ContentValues();

                if (mCurrentQuantity - 1 < 0) {
                    Toast.makeText(context, "Insufficient quantity to complete order", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateValues.put(ProductEntry.COLUMN_QUANTITY, mCurrentQuantity - 1);

                int rowsAffected = context.getContentResolver().update(mCurrentProductUri, updateValues, null, null);

                if (rowsAffected > 0) {
                    Toast.makeText(context, "Quantity has been decrease", Toast.LENGTH_SHORT).show();
                    context.getContentResolver().notifyChange(mCurrentProductUri, null);
                } else {
                    Toast.makeText(context, "Could not update quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        mQuantityTextView.setText(Integer.toString(mCurrentQuantity));
        mPriceTextView.setText(Integer.toString(productPrice));

    }


}
