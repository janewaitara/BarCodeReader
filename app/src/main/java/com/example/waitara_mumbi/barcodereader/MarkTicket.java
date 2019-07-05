package com.example.waitara_mumbi.barcodereader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MarkTicket {

    @SerializedName("status_code")
    @Expose
    private  String statusCode;

    public String getStatusCode() {
        return statusCode;
    }
}
