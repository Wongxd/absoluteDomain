
public interface Contracts {
    interface Menu {
        String MENU_THEME = "主题";
        int MENU_THEME_ID = 999;
        String MENU_MORE = "更多";
        int MENU_MORE_ID = 998;


        String MENU_YANDE = "YANDE";
        int MENU_YANDE_ID = 1;

        String MENU_KONACHAN = "KONACHAN";
        int MENU_KONACHAN_ID = 2;

        String MENU_LOLIBOORU = "LOLIBOORU";
        int MENU_LOLIBOORU_ID = 3;

        String MENU_DANBOORU = "DANBOORU";
        int MENU_DANBOORU_ID = 4;

        String MENU_SAFEBOORU = "SAFEBOORU";
        int MENU_SAFEBOORU_ID = 5;

        String MENU_E621 = "E621";
        int MENU_E621_ID = 6;

        String MENU_WALLHAVEN = "WALLHAVEN";
        int MENU_WALLHAVEN_ID = 10;

        String MENU_GANK = "GANK";
        int MENU_GANK_ID = 11;

        String MENU_BING = "BING";
        int MENU_BING_ID = 12;

        String MENU_TNGOU = "TNGOU";
        int MENU_TNGOU_ID = 13;

        String MENU_GACHA = "GACHA";
        int MENU_GACHA_ID = 14;

        String MENU_BCY_SELECTED = "BCY SELECTED";
        int MENU_BCY_ILLUST_SELECTED_ID = 15;
        int MENU_BCY_COS_SELECTED_ID = 16;

        String MENU_BCY = "BCY";
        String MENU_BCY_RANKING = "BCY RANKING";
        int MENU_BCY_ILLUST_RANKING_ID = 17;
        int MENU_BCY_COS_RANKING_ID = 18;

        String MENU_MANGA_DRAWING = "MANGA DRAWING";
        int MENU_MANGA_DRAWING_ID = 19;
        String MENU_MANGA_DRAWING_HENTAI = "MANGA DRAWING HENTAI";
        int MENU_MANGA_DRAWING_HENTAI_ID = 20;

        String MENU_MAG_MOE = "MAG MOE";
        int MENU_MAG_MOE_MOE_ID = 21;
        int MENU_MAG_MOE_COS_STAR_ID = 22;

        String MENU_APIC = "APIC";
        int MENU_APIC_ID = 23;

        String MENU_ZEROCHAN = "ZEROCHAN";
        int MENU_ZEROCHAN_ID = 24;

        String MENU_E_SHUUSHUU = "E-SHUUSHUU";
        int MENU_E_SHUUSHUU_ID = 25;

        String MENU_MINITOKYO = "MINITOKYO";
        int MENU_MINITOKYO_ID = 26;

        String MENU_WWW_005_TV = "WWW.005.TV";
        int MENU_WWW_005_TV_ACG_ID = 27;
        int MENU_WWW_005_TV_COS_ID = 28;

        String MENU_ORZ_17173_COM = "ORZ.17173";
        int MENU_ORZ_17173_COM_ID = 29;

        String MENU_JDLINGYU = "JDLINGYU";
        int MENU_JDLINGYU_ACG_ID = 30;
        int MENU_JDLINGYU_MEIZHI_ID = 31;

        String MENU_LESMAO = "LESMAO";
        int MENU_LESMAO_ID = 32;

        String MENU_XIUMM = "XIUMM";
        int MENU_XIUMM_ID = 33;

        String MENU_94TAOTU = "94TAOTU";
        int MENU_94TAOTU_ID = 34;

        String MENU_MZITU = "MZITU";
        int MENU_MZITU_ID = 35;

        String MENU_MMONLY = "MMONLY";
        int MENU_MMONLY_ID = 36;

        String MENU_ROSIYY = "ROSIYY";
        int MENU_ROSIYY_ID = 37;

        String MENU_192TT = "192TT";
        int MENU_192TT_ID = 38;

        String MENU_XIUREN = "XIUREN";
        int MENU_XIUREN_ID = 39;

        String MENU_LINGYU = "LINGYU";
        int MENU_LINGYU_ID = 40;

        String MENU_MOEIMG = "MOEIMG";
        int MENU_MOEIMG_ID = 41;

        String MENU_NIJIERO_CH = "NIJIERO-CH";
        int MENU_NIJIERO_CH_ID = 42;

        String MENU_COSPLAY_LA = "COSPLAY.LA";
        int MENU_COSPLAY_LA_ID = 43;

        String MENU_CHINAGIRLOL = "CHINAGIRLOL";
        int MENU_CHINAGIRLOL_ID_MZ = 44;
        int MENU_CHINAGIRLOL_ID_COS = 45;

        String MENU_MOE005TV = "MOE.005.TV";
        int MENU_MOE005TV_ACG_ID = 46;
        int MENU_MOE005TV_COS_ID = 47;

        String MENU_ACG_GAMERSKY = "ACG.GAMERSKY";
        int MENU_ACG_GAMERSKY_ACG_ID = 48;
        int MENU_ACG_GAMERSKY_COS_ID = 49;

        String MENU_PANGCI = "PANGCI";
        int MENU_PANGCI_ID = 50;

    }

    interface Url {
        String HITOKOTO_BILIBILIJJ = "http://hitokoto.bilibilijj.com/?status=get";
        String HITOKOTO_BILIBILIJJ_BASE = "http://www.jijidown.com/";
        String HITOKOTO_IMJAD = "https://api.imjad.cn/hitokoto/?encode=json&charset=utf-8";
        String HITOKOTO_IMJAD_BASE = "https://imjad.cn/";
        String HITOKOTO_LOLI="https://api.satori.moe/hitokoto.php";
        String HITOKOTO_LOLI_BASE="https://satori.moe";
        String YIJU = "http://yiju.ml/api/word.php";
        String YIJU_BASE = "http://yiju.ml/";

        String TODAY_IN_HISTORY = "http://kabe.im/";

        String YANDE = "https://yande.re/post.json?limit=24&page=";

        String KONACHAN = "https://konachan.com/post.json?limit=24&page=";

        String LOLIBOORU = "https://lolibooru.moe/post/index.json?limit=24&page=";

        String DANBOORU = "https://danbooru.donmai.us/posts.json?limit=24&page=";
        String DANBOORU_BASE = "https://danbooru.donmai.us";

        String SAFEBOORU = "https://safebooru.donmai.us/posts.json?limit=24&page=";
        String SAFEBOORU_BASE = "https://safebooru.donmai.us";

        String E621 = "https://e621.net/post/index.json?limit=24&page=";

        String WALLHAVEN = "https://alpha.wallhaven.cc/latest?page=";

        String GANK = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/24/";

        String BING = "http://cn.bing.com/HPImageArchive.aspx?format=js&n=24";
        String BING_BASE = "http://cn.bing.com";

        String TNGOU_BASE = "http://tnfs.tngou.net/image";
        String TNGOU_CLASSIFY = "http://www.tngou.net/tnfs/api/classify";
        String TNGOU_LIST = "http://www.tngou.net/tnfs/api/list?rows=24&id=";

        String GACHA_DAY = "http://gacha.163.com/ranking/pic/day/current";
        String GACHA_WEEK = "http://gacha.163.com/ranking/pic/week/current";
        String GACHA_MONTH = "http://gacha.163.com/ranking/pic/month/current";

        String BCY_BASE = "https://bcy.net";
        String BCY_ILLUST_SELECTED = "https://bcy.net/illust/discover?&p=";
        String BCY_COS_SELECTED = "https://bcy.net/coser/discover?&p=";
        String BCY_ILLUST_RANK_TODAY = "http://bcy.net/illust/toppost100?type=lastday";
        String BCY_ILLUST_RANK_WEEK = "http://bcy.net/illust/toppost100";
        String BCY_ILLUST_RANK_NEW_PEOPLE = "http://bcy.net/illust/toppost100?type=newPeople";
        String BCY_ILLUST_RANK_ART_WORK = "http://bcy.net/illust/toppost100?type=artwork";
        String BCY_COS_RANK_TODAY = "http://bcy.net/coser/toppost100?type=lastday";
        String BCY_COS_RANK_WEEK = "http://bcy.net/coser/toppost100";
        String BCY_COS_RANK_NEW_PEOPLE = "http://bcy.net/coser/toppost100?type=newPeople";
        String BCY_COS_RANK_ART_WORK = "http://bcy.net/coser/toppost100?type=artwork";

        String MANGA_DRAWING_ALL = "http://mangadrawing.net/gallery?page=";
        String MANGA_DRAWING_IMAGES = "http://mangadrawing.net/gallery/images?page=";
        String MANGA_DRAWING_ARTWORKS = "http://mangadrawing.net/gallery/artworks?page=";
        String MANGA_DRAWING_FAVORITES = "http://mangadrawing.net/gallery/favorites?page=";
        String MANGA_DRAWING_DOWNLOADS = "http://mangadrawing.net/gallery/downloads?page=";
        String MANGA_DRAWING_POPULAR = "http://mangadrawing.net/gallery/views?page=";
        String MANGA_DRAWING_HENTAI_ALL = "http://hentai.mangadrawing.net/gallery?page=";
        String MANGA_DRAWING_HENTAI_IMAGES = "http://hentai.mangadrawing.net/gallery/images?page=";
        String MANGA_DRAWING_HENTAI_UNRECOGNIZED = "http://hentai.mangadrawing.net/gallery/unrecognized?page=";
        String MANGA_DRAWING_HENTAI_FAVORITES = "http://hentai.mangadrawing.net/gallery/favorites?page=";
        String MANGA_DRAWING_HENTAI_DOWNLOADS = "http://hentai.mangadrawing.net/gallery/downloads?page=";
        String MANGA_DRAWING_HENTAI_POPULAR = "http://hentai.mangadrawing.net/gallery/views?page=";

        String MAG_MOE_MOE = "http://mag.moe/category/images/page/";
        String MAG_MOE_COSPLAY = "http://mag.moe/category/cosplay/page/";
        String MAG_MOE_STAR = "http://mag.moe/category/celebrity/page/";

        String APIC_ALL = "http://www.apic.in/page/";
        String APIC_ANIME = "http://www.apic.in/anime/page/";
        String APIC_ZHIFU = "http://www.apic.in/zhifu/page/";
        String APIC_HENTAI = "http://www.apic.in/hentai/page/";
        String APIC_YUSANJIA = "http://www.apic.in/yusanjia/page/";
        String APIC_ZATUJI = "http://www.apic.in/zatuji/page/";
        String APIC_FULI = "http://www.apic.in/fuli/page/";

        String ZEROCHAN = "http://www.zerochan.net/?p=";

        String ZEROCHAN_POPULAR = "http://www.zerochan.net/popular";

        String E_SHUUSHUU = "http://e-shuushuu.net/?page=";
        String E_SHUUSHUU_BASE = "http://e-shuushuu.net";

        String MINITOKYO = "http://gallery.minitokyo.net/wallpapers?order=id&tid=&elite=0&highlight=0&dim=&display=thumbnails&page=";

        String WWW_005_TV_P = "http://www.005.tv/Cosplay/meizhouPzhanbenzitu/list_633_";
        String WWW_005_TV_ACG = "http://www.005.tv/Cosplay/erciyuanmeitu/list_632_";
        String WWW_005_TV_COS = "http://www.005.tv/Cosplay/Cosplay/list_631_";

        String ORZ_17173_COM = "http://orz.17173.com/illusion/index_";

        String JDLINGYU_ALL = "http://www.jdlingyu.moe/page/";
        String JDLINGYU_ZHUANTI = "http://www.jdlingyu.moe/%E4%B8%93%E9%A2%98/page/";
        String JDLINGYU_TEDIAN = "http://www.jdlingyu.moe/%E7%89%B9%E7%82%B9/page/";
        String JDLINGYU_NONGCAO = "http://www.jdlingyu.moe/%E5%BC%84%E6%BD%AE/page/";
        String JDLINGYU_COSPLAY = "http://www.jdlingyu.moe/cosplay/page/";
        String JDLINGYU_XIEZHEN = "http://www.jdlingyu.moe/%E5%86%99%E7%9C%9F/page/";
        String JDLINGYU_XIAZAI = "http://www.jdlingyu.moe/%E4%B8%8B%E8%BD%BD/page/";
        String JDLINGYU_ACG = "http://www.jdlingyu.moe/acg/page/";

        String LESMAO = "http://www.lesmao.com/portal.php?page=";

        String XIUMM = "http://www.xiumm.org/albums/page-";
        String XIUMM_BASE = "http://www.xiumm.org";

        String WWW94TAOTUCOM = "http://www.94taotu.com/albums/page-";
        String WWW94TAOTUCOM_BASE = "http://www.94taotu.com";

        String MZITU = "http://www.mzitu.com/page/";

        String MMONLY = "http://www.mmonly.cc/mmtp/list_9_";

        String ROSIYY = "http://www.rosiyy.com/index-";
        String ROSIYY_BASE = "http://www.rosiyy.com";

        String WWW192TTCOM_MT = "http://www.192tt.com/listinfo-1-";
        String WWW192TTCOM_GQ = "http://www.192tt.com/listinfo-34-";
        String WWW192TTCOM_BASE = "http://www.192tt.com";

        String XIUREN = "http://www.xiuren.org/page-";

        String LINGYU = "http://www.lingyu.me/page/";

        String MOEIMG = "http://moeimg.net/category/%E9%9D%9E%E3%82%A8%E3%83%AD%E3%83%BB%E5%BE%AE%E3%82%A8%E3%83%AD%E7%94%BB%E5%83%8F/page/";
        String MOEIMG_H = "http://moeimg.net/page/";

        String NIJIERO_CH = "https://nijiero-ch.com/page/";

        String COSPLAY_LA = "http://cosplay.la/photo/index/2-0-";
        String COSPLAY_LA_BASE = "http://cosplay.la";

        String CHINAGIRLOL_BASE = "http://www.chinagirlol.cc/";
        String CHINAGIRLOL_MN = "http://www.chinagirlol.cc/forum.php?mod=forumdisplay&fid=112&orderby=lastpost&orderby=lastpost&filter=lastpost&page=";
        String CHINAGIRLOL_MT = "http://www.chinagirlol.cc/forum.php?mod=forumdisplay&fid=66&orderby=lastpost&filter=lastpost&orderby=lastpost&page=";
        String CHINAGIRLOL_COS = "http://www.chinagirlol.cc/forum.php?mod=forumdisplay&fid=99&orderby=lastpost&filter=lastpost&orderby=lastpost&page=";

        String MOE005TV_MT = "http://moe.005.tv/moeimg/tb/list_3_";
        String MOE005TV_DNBZ = "http://moe.005.tv/moeimg/bz/list_4_";
        String MOE005TV_SJBZ = "http://moe.005.tv/moeimg/sjbz/list_601_";
        String MOE005TV_COSER = "http://moe.005.tv/cosplay/cosjs/list_600_";
        String MOE005TV_COS = "http://moe.005.tv/cosplay/cosxztj/list_598_";

        String ACG_GAMERSKY_ZX = "http://db2.gamersky.com/LabelJsonpAjax.aspx?jsondata={\"type\":\"updatenodelabel\",\"isCache\":true,\"cacheTime\":60,\"nodeId\":\"20449\",\"isNodeId\":\"true\",\"page\":";
        String ACG_GAMERSKY_PC = "http://db2.gamersky.com/LabelJsonpAjax.aspx?jsondata={\"type\":\"updatenodelabel\",\"isCache\":true,\"cacheTime\":60,\"nodeId\":\"20450\",\"isNodeId\":\"true\",\"page\":";
        String ACG_GAMERSKY_SJ = "http://db2.gamersky.com/LabelJsonpAjax.aspx?jsondata={\"type\":\"updatenodelabel\",\"isCache\":true,\"cacheTime\":60,\"nodeId\":\"20451\",\"isNodeId\":\"true\",\"page\":";
        String ACG_GAMERSKY_MT = "http://db2.gamersky.com/LabelJsonpAjax.aspx?jsondata={\"type\":\"updatenodelabel\",\"isCache\":true,\"cacheTime\":60,\"nodeId\":\"20395\",\"isNodeId\":\"true\",\"page\":";
        String ACG_GAMERSKY_COS = "http://db2.gamersky.com/LabelJsonpAjax.aspx?jsondata={\"type\":\"updatenodelabel\",\"isCache\":true,\"cacheTime\":60,\"nodeId\":\"20397\",\"isNodeId\":\"true\",\"page\":";

        String PANGCI = "https://www.pangci.cc/works/index_";

        String MM131_XG = "http://www.mm131.com/xinggan/list_6_";
        String MM131_QC = "http://www.mm131.com/qingchun/list_1_";
        String MM131_XH = "http://www.mm131.com/xiaohua/list_2_";
        String MM131_CM = "http://www.mm131.com/chemo/list_3_";
        String MM131_QP = "http://www.mm131.com/qipao/list_4_";
        String MM131_XQ = "http://www.mm131.com/mingxing/list_5_";

    }

    interface LockType {
        int NONE = 0;
        int PIN = 1;
        int PATTARN = 2;
    }

}
