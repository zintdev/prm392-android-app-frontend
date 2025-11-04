package com.example.prm392_android_app_frontend.data.dto;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CartItemDto implements Parcelable {

    @SerializedName("cartItemId")
    private int cartItemId;

    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("unitPrice")
    private double unitPrice;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("selected")
    private boolean selected;

    @SerializedName("currency")
    private String currency;

    @SerializedName("taxRate")
    private double taxRate;

    // Constructor mặc định cho Gson
    public CartItemDto() {}

    // Constructor cho Parcelable
    protected CartItemDto(Parcel in) {
        cartItemId = in.readInt();
        productId = in.readInt();
        productName = in.readString();
        imageUrl = in.readString();
        unitPrice = in.readDouble();
        quantity = in.readInt();
        selected = in.readByte() != 0;
        currency = in.readString();
        taxRate = in.readDouble();
    }

    public static final Creator<CartItemDto> CREATOR = new Creator<CartItemDto>() {
        @Override
        public CartItemDto createFromParcel(Parcel in) {
            return new CartItemDto(in);
        }

        @Override
        public CartItemDto[] newArray(int size) {
            return new CartItemDto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cartItemId);
        dest.writeInt(productId);
        dest.writeString(productName);
        dest.writeString(imageUrl);
        dest.writeDouble(unitPrice);
        dest.writeInt(quantity);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(currency);
        dest.writeDouble(taxRate);
    }

    // --- Getters and Setters ---
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSelected(boolean selected) { this.selected = selected; }


    // (Tạo getters và setters cho tất cả các trường)

    public int getCartItemId() { return cartItemId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getImageUrl() { return imageUrl; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return selected; }
    public String getCurrency() { return currency; }
    public double getTaxRate() { return taxRate; }
}

