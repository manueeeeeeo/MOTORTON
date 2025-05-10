package com.clase.motorton.bd;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoConection {
    private static final String CONNECTION_STRING = "mongodb+srv://manupruebaan49:Al0SfGAltCPt2cbK@cluster0.v2ekckb.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    public static MongoClient getMongoClient() {
        return MongoClients.create(CONNECTION_STRING);
    }
}