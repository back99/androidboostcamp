package org.techtown.androidboostcamp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.techtown.androidboostcamp.data.MovieInfo;
import org.techtown.androidboostcamp.data.MovieList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    RecyclerView recyclerView;
    MovieAdapter movieAdapter;
    String query;
    Handler handler = new Handler();
    MovieAdapter adapter;
    StringBuffer response;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        movieAdapter = new MovieAdapter(getApplicationContext());


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                query = editText.getText().toString();

                new Thread(new Runnable() {

                    @Override
                    public void run() {


                        try {
                            String clientId = "DhU8jxH1HY7DqrUlVmrr";//애플리케이션 클라이언트 아이디값";
                            String clientSecret = "ErVxxUCc10";//애플리케이션 클라이언트 시크릿값";
                            String text = URLEncoder.encode(query, "UTF-8");
                            String apiURL = "https://openapi.naver.com/v1/search/movie?query=" + text; // json 결과
                            URL url = new URL(apiURL);
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("GET");
                            con.setRequestProperty("X-Naver-Client-Id", clientId);
                            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                            int responseCode = con.getResponseCode();

                            BufferedReader br;
                            if (responseCode == 200) { // 정상 호출
                                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            } else {  // 에러 발생
                                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                            }
                            String inputLine;
                            response = new StringBuffer();
                            while ((inputLine = br.readLine()) != null) {
                                response.append(inputLine);
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    processResponse(response.toString());

                                }
                            });
                            br.close();
                        } catch (Exception e) {
                            System.out.println(e);

                        }

                    }


                }).start();

            }
        });
    }


    public void processResponse(String response) {
        Gson gson = new Gson();
        response=response.replaceAll("<b>","");
        response=response.replaceAll("</b>","");


        MovieList movieList = gson.fromJson(response, MovieList.class);
        adapter=new MovieAdapter(getApplicationContext());

        if(movieList.items.size()==0)
            Toast.makeText(getApplicationContext(),"입력이 잘못되었습니다.",Toast.LENGTH_LONG).show();

        for (int i = 0; i < movieList.items.size(); i++) {
            MovieInfo movieInfo = movieList.items.get(i);
            adapter.addItem(new MovieItem(movieInfo.title,movieInfo.link,movieInfo.image,movieInfo.pubDate,movieInfo.director,movieInfo.actor,movieInfo.userRating));
        }
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieAdapter.ViewHolder holder, View view, int position) {
                MovieItem item=adapter.getItem(position);
                String url=item.getLink();//이 주소로 링크 띄우면 댐!!!
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
            }
        });
    }
}

