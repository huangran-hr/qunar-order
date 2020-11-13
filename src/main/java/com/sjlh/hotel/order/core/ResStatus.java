package com.sjlh.hotel.order.core;

import lombok.Data;

/**
 * @Auther: huangran
 * @Date: 20120/4/2 11:35
 * @Description:
 */
public enum ResStatus {

    //订单状态代码（0001：RESERVED ；0003：CANCELED ；0004：CHECK IN；0005：WAITING；0006：CHECK OUT ；
    // 0007：NO SHOW；0008：REJECTED；）

    CANCELED("0003", "Cancelled"),
    CHECKIN("0004", "InHouse"),
    CHECKOUT("0006", "CheckedOut"),
    NOSHOW("007", "NoShow");


    private String status;
    private String name;

    ResStatus(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ResStatus getCrsOrderStatus(String status) {
        ResStatus rs = null;
        for (ResStatus r : values()) {
            if (r.getStatus() == status) {
                rs = r;
                break;
            }
        }
        if (rs == null) throw new IllegalArgumentException("room set status " + status + " not defined");
        return rs;
    }

    public static String getName(String status) {
        for (ResStatus r : values()) {
            if(r.getStatus().equals(status)){
                return r.getName();
            }
        }
        return null;
    }
}
