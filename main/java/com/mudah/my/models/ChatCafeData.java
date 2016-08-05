package com.mudah.my.models;

import com.chatcafe.sdk.model.CCBuyer;
import com.chatcafe.sdk.model.CCProduct;
import com.chatcafe.sdk.model.CCRoomDetail;
import com.chatcafe.sdk.model.CCSeller;
import com.lib701.datasets.ACAd;
import com.lib701.utils.ACUtils;
import com.lib701.utils.Log;
import com.mudah.my.configs.Config;
import com.mudah.my.configs.Constants;

import java.util.ArrayList;


public class ChatCafeData {

    private ACAd dfAdsDO;

    public ChatCafeData(ACAd dfAdsDO) {
        this.dfAdsDO = dfAdsDO;
    }

    public CCRoomDetail getRoomDetail() {

        String productThumbnail = Constants.EMPTY_STRING;
        Log.d("product image count: " + dfAdsDO.getImageCount());
        if (dfAdsDO.getImageCount() > 0) {
            //use thumbnails first. If no thumbnail, use the real image
            ArrayList<String> thumbs = dfAdsDO.getThumbs();
            if (thumbs.size() > 0) {
                productThumbnail = thumbs.get(0);
            }
            if (ACUtils.isEmpty(productThumbnail)) {
                ArrayList<String> images = dfAdsDO.getImages();
                if (images.size() > 0) {
                    productThumbnail = images.get(0);
                }
            }
        }

        CCProduct ccProduct = new CCProduct(
                dfAdsDO.getListId() + Constants.EMPTY_STRING,
                dfAdsDO.getSubject(),
                productThumbnail,
                dfAdsDO.getPrice()
        );

        CCBuyer ccBuyer = new CCBuyer(
                Config.userAccount.getUserId(),
                Config.userAccount.getUsername(),
                Constants.EMPTY_STRING
        );

        CCSeller ccSeller = new CCSeller(
                dfAdsDO.getUserId(),
                dfAdsDO.getName(),
                Constants.EMPTY_STRING
        );

        return new CCRoomDetail(ccProduct, ccBuyer, ccSeller);
    }
}
