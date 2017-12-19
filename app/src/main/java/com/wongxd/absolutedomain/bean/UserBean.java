package com.wongxd.absolutedomain.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by wongxd on 2017/12/19.
 */

public class UserBean extends BmobUser {

    //图集收藏
    private String favorite;

    public String getFavorite() {
        return favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }
}
