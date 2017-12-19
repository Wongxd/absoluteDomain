package com.wongxd.absolutedomain.bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by wongxd on 2017/12/19.
 */

public class UserBean extends BmobUser {

    //图集收藏
    private BmobFile favorite;

    public BmobFile getFavorite() {
        return favorite;
    }

    public void setFavorite(BmobFile favorite) {
        this.favorite = favorite;
    }
}
