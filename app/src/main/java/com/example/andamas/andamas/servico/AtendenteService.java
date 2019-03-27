package com.example.andamas.andamas.servico;

import com.example.andamas.andamas.modelo.IpClasse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AtendenteService {
    @GET(".")
    Call<List<IpClasse>> getIp();
}
