package com.example.andamas.andamas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase bancoDeDados;

    private ArrayAdapter<String> incidentesAdaptador;
    private ArrayList<String> incidentes;
    private ArrayList<Integer> idsIncidentes;

    private ImageView imagemCriar;
    private ListView listaIncidentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listaIncidentes = (ListView) findViewById(R.id.listaIncidentes);
        imagemCriar = (ImageView) findViewById(R.id.imagemCriar);

        imagemCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, FormularioIncidente.class);
                startActivity(it);
            }
        });

        listaIncidentes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent it = new Intent(MainActivity.this, FormularioIncidente.class);
                it.putExtra("id", idsIncidentes.get(i));
                startActivity(it);
            }
        });

        try {
            bancoDeDados = openOrCreateDatabase("andamas", MODE_PRIVATE, null);

            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS atendentes (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    " nome VARCHAR(64) NOT NULL, CONSTRAINT id_unique UNIQUE (id))");
            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS clientes (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    " nome varchar(64) NOT NULL, empresa varchar(64) NOT NULL, CONSTRAINT id_unique UNIQUE (id))");
            bancoDeDados.execSQL("CREATE TABLE IF NOT EXISTS incidentes (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    " atendente INT NOT NULL, cliente INT NOT NULL, descricao VARCHAR(512) DEFAULT NULL," +
                    " status VARCHAR(16) DEFAULT NULL, creation_time TIMESTAMP DEFAULT NULL, CONSTRAINT id_unique UNIQUE (id)," +
                    " CONSTRAINT fk_atendente FOREIGN KEY (atendente) REFERENCES atendentes (id) ON DELETE NO ACTION " +
                    " ON UPDATE NO ACTION," +
                    " CONSTRAINT fk_cliente FOREIGN KEY (cliente) REFERENCES clientes (id) ON DELETE NO ACTION" +
                    " ON UPDATE NO ACTION)");

            bancoDeDados.execSQL("CREATE INDEX IF NOT EXISTS fk_incidentes_atendente_idx ON incidentes (atendente)");
            bancoDeDados.execSQL("CREATE INDEX IF NOT EXISTS fk_incidentes_clientes_idx ON incidentes (cliente)");

            //bancoDeDados.execSQL("INSERT INTO atendentes (id, nome) VALUES (4, 'Anderson'), (5, 'André'), (6, 'Otávio')");
            /*bancoDeDados.execSQL("INSERT INTO clientes (id, nome, empresa) VALUES (1, 'Jefferson', 'Pixel Inc')," +
                    " (2, 'Máximo', 'York Research'), (3, 'Gabriella', 'Faraday Co')");*/
            /*bancoDeDados.execSQL("INSERT INTO incidentes (id, atendente, cliente, descricao, status, creation_time) VALUES" +
                    " (1, 4, 2, 'Desc do problema', 'aberto', '2018-06-19 01:12:48')");*/

            recuperarIncidentes();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recuperarIncidentes() {

        try {
            final Cursor cursor = bancoDeDados.rawQuery("SELECT * FROM incidentes ORDER BY id DESC", null);
            SimpleDateFormat diaFormato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            incidentes = new ArrayList<String>();
            idsIncidentes = new ArrayList<Integer>();
            incidentesAdaptador = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.list_incidentes,
                    R.id.list_incidents,
                    incidentes) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view =  super.getView(position, convertView, parent);
                    Cursor c2 = bancoDeDados.rawQuery("SELECT * FROM incidentes WHERE id = " + idsIncidentes.get(position),
                            null);
                    Log.i("position", "" + position);
                    c2.moveToFirst();
                    if(c2.getString(c2.getColumnIndex("status")).equals("aberto")) view.setBackgroundColor(Color.RED);
                    else if(c2.getString(c2.getColumnIndex("status")).equals("encerrado")) view.setBackgroundColor(Color.GREEN);

                    return view;
                }
            };
            listaIncidentes.setAdapter(incidentesAdaptador);
            cursor.moveToFirst();
            for(int i = 0; i < cursor.getCount(); i++) {
                incidentes.add("Incidente " + cursor.getInt(cursor.getColumnIndex("id")));
                idsIncidentes.add(cursor.getInt(cursor.getColumnIndex("id")));
                cursor.moveToNext();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
