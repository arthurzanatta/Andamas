package com.example.andamas.andamas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.andamas.andamas.modelo.Incidente;
import com.example.andamas.andamas.modelo.IpClasse;
import com.example.andamas.andamas.servico.AtendenteService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormularioIncidente extends AppCompatActivity {

    private EditText textoAtendente;
    private EditText textoCliente;
    private EditText textoDescricao;
    private EditText textoCreationTime;
    private Button botaoRegistrar;
    private Switch switchStatus;
    private ImageView imagemCancelar;

    private final String URL_IP = "https://api.ipify.org/?format=json";
    //private final String FORMATO = "json";
    private List<IpClasse> listaIP = new ArrayList<>();
    private List<Incidente> listaIncidente = new ArrayList<>();
    private SQLiteDatabase bancoDeDados;
    private Integer idIncidente = -1;

    String ipAtendente = "\n\n IP do Atendente: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_incidente);

        textoAtendente = (EditText) findViewById(R.id.textoAtendente);
        textoCliente = (EditText) findViewById(R.id.textoCliente);
        textoDescricao = (EditText) findViewById(R.id.textoDescricao);
        textoCreationTime = (EditText) findViewById(R.id.textoCreationTime);
        imagemCancelar = (ImageView) findViewById(R.id.imagemCancelar);
        botaoRegistrar = (Button) findViewById(R.id.botaoRegistrar);
        switchStatus = (Switch) findViewById(R.id.switchStatus);

        textoCreationTime.setEnabled(false);

        Bundle idIncidenteBundle = getIntent().getExtras();
        if(idIncidenteBundle != null) idIncidente = idIncidenteBundle.getInt("id");
        else {
            SimpleDateFormat formatCreationTime = new SimpleDateFormat("yyyy-MM-dd");
            Date dataCreationTime = Calendar.getInstance().getTime();
            String dataFormatadaCT = formatCreationTime.format(dataCreationTime);
            textoCreationTime.setText(dataFormatadaCT);
        }

        imagemCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FormularioIncidente.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        switchStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchStatus.isChecked()) switchStatus.setText("Encerrado");
                else switchStatus.setText("Aberto");
            }
        });

        if(idIncidente > -1) {
            try {
                bancoDeDados = openOrCreateDatabase("andamas", MODE_PRIVATE, null);
                carregarIncidente();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        botaoRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(idIncidente == -1) {
                    //Obtenção do IP do(a) Atendente
                    Call<List<IpClasse>> ips = consultaAPI();
                    ips.enqueue(new Callback<List<IpClasse>>() {
                        @Override
                        public void onResponse(Call<List<IpClasse>> call, Response<List<IpClasse>> response) {
                            if(response.isSuccessful()) listaIP = response.body();
                            if(listaIP != null) {
                                for(IpClasse ipNumber : listaIP) {
                                    ipAtendente = "" + ipNumber.getIp();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<IpClasse>> call, Throwable t) {
                            Toast.makeText(FormularioIncidente.this, "Falha ao obter resposta da API", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                    try {
                        bancoDeDados = openOrCreateDatabase("andamas", MODE_PRIVATE, null);
                        salvarIncidente();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        alterarIncidente();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void salvarIncidente() {

        String status;
        if(switchStatus.isChecked()) status = "encerrado";
        else status = "aberto";

        SimpleDateFormat formataData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date data = Calendar.getInstance().getTime();
        String dataFormatada = formataData.format(data);

        try {
            if(!textoAtendente.getText().toString().equals("") && !textoCliente.getText().toString().equals("")) {
                bancoDeDados.execSQL("INSERT INTO incidentes (atendente, cliente, descricao, status, creation_time) VALUES ("
                        + Integer.parseInt(textoAtendente.getText().toString()) + ", "
                        + Integer.parseInt(textoCliente.getText().toString()) + ", '"
                        + textoDescricao.getText().toString() + "', '"
                        + status + "', '"
                        + dataFormatada + "')");
                Toast.makeText(FormularioIncidente.this, "Incidente registrado com sucesso!",
                        Toast.LENGTH_LONG).show();
                Intent it = new Intent(FormularioIncidente.this, MainActivity.class);
                startActivity(it);
            }
            else if(textoCliente.getText().toString().equals("")) Toast.makeText(FormularioIncidente.this,
                    "Insira o id do cliente", Toast.LENGTH_LONG).show();
            else if(textoAtendente.getText().toString().equals("")) Toast.makeText(FormularioIncidente.this,
                    "Insira o id do atendente", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void carregarIncidente() {
        try {
            Cursor c = bancoDeDados.rawQuery("SELECT * FROM incidentes WHERE id = " + idIncidente, null);
            c.moveToFirst();
            textoAtendente.setText("" + c.getInt(c.getColumnIndex("atendente")));
            textoCliente.setText("" + c.getInt(c.getColumnIndex("cliente")));
            textoDescricao.setText(c.getString(c.getColumnIndex("descricao")));
            textoCreationTime.setText(c.getString(c.getColumnIndex("creation_time")));
            if(c.getString(c.getColumnIndex("status")).equals("encerrado")) {
                switchStatus.setChecked(true);
                switchStatus.setText("Encerrado");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alterarIncidente() {
        String status;

        if(switchStatus.isChecked()) status = "encerrado";
        else status = "aberto";

        try {
            if(!textoAtendente.getText().toString().equals("") && !textoCliente.getText().toString().equals("")) {
                bancoDeDados.execSQL("UPDATE incidentes SET " +
                        "atendente = " + Integer.parseInt(textoAtendente.getText().toString()) + ", " +
                        "cliente = " + Integer.parseInt(textoCliente.getText().toString()) + ", " +
                        "descricao = '" + textoDescricao.getText().toString() + "', " +
                        "status = '" + status + "' WHERE id = " + idIncidente + ";");
                Toast.makeText(FormularioIncidente.this, "Incidente alterado com sucesso!",
                        Toast.LENGTH_LONG).show();
                Intent it = new Intent(FormularioIncidente.this, MainActivity.class);
                startActivity(it);
            }
            else if(textoCliente.getText().toString().equals("")) Toast.makeText(FormularioIncidente.this,
                    "Insira o id do cliente", Toast.LENGTH_LONG).show();
            else if(textoAtendente.getText().toString().equals("")) Toast.makeText(FormularioIncidente.this,
                    "Insira o id do atendente", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Call<List<IpClasse>> consultaAPI() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(URL_IP).addConverterFactory(GsonConverterFactory.create()).build();

        AtendenteService ipService = retrofit.create(AtendenteService.class);
        Call<List<IpClasse>> ips = ipService.getIp();

        return ips;
    }

}
