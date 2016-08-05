package com.example.httpclienttest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * ʹ��HttpClient�������󡢽�����Ӧ�ܼ򵥣�ֻҪ���¼�����
 * 1������HttpClient����
 * 2�������Ҫ����Get���󣬴���HttpGet���������Ҫ����Post���󣬴���HttpPost����
 * 3�������Ҫ��������������ɵ���HttpGet��HttpPost��ͬ��setParams(HttpParams params)������������������
 *    ����HttpPost������ԣ�Ҳ�ɵ���setEntity(HttpEntity entity)�������������������
 * 4������HttpClient�����execute(HttpUriRequest request)��������ִ�и÷�������һ��HttpResponse��
 * 5������HttpResponse��getAllHeaders()��getHeaders(String name)�ȷ����ɻ�ȡ����������Ӧͷ��
 *    ����HttpResponse��getEntity()�����ɻ�ȡHttpEntity���󣬸ö����װ�˷���������Ӧ���ݡ�
 *    �����ͨ���ö����ȡ����������Ӧ���ݡ�
 */
public class HttpClientTest extends Activity {
	
	private TextView response;
	HttpClient httpClient;
	
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			if (msg.what == 0x123)
			{
				response.append(msg.obj.toString() + "\n");//ʹ��response�ı�����ʾ��������Ӧ
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        httpClient = new DefaultHttpClient(); //����DefaultHttpClient����
        response = (TextView)findViewById(R.id.response);
    }
    
    public void accessSecret(View v) 
    {
		response.setText("");
		new Thread()
		{
			@Override
			public void run() 
			{
				HttpGet get = new HttpGet("http://192.168.1.88:8888/foo/secret.jsp");//����һ��HttpGet����
				try 
				{
					HttpResponse httpResponse = httpClient.execute(get); //����get����
					HttpEntity entity = httpResponse.getEntity();
					
					if (entity != null)
					{
						//��ȡ��������Ӧ
						BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
						String line = null;
						
						while ((line = br.readLine()) != null)
						{
							Message message = new Message();
							message.what = 0x123;
							message.obj = line;
							handler.sendMessage(message);
						}
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}.start();
	}
    
    public void showLogin(View v) 
    {
		final View loginDialog = getLayoutInflater().inflate(R.layout.login, null);//���ص�¼����
		
		//ʹ�öԻ����û���¼ϵͳ
		new AlertDialog.Builder(HttpClientTest.this)
			.setTitle("��¼ϵͳ")
			.setView(loginDialog)
			.setPositiveButton("��¼", new DialogInterface.OnClickListener() 
			{	
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					//��ȡ�û�������û���������
					final String name = ((EditText)loginDialog.findViewById(R.id.name)).getText().toString();
					final String password = ((EditText)loginDialog.findViewById(R.id.password)).getText().toString();
					
					new Thread()
					{
						@Override
						public void run() 
						{
							try 
							{
								HttpPost post = new HttpPost("http://192.168" + ".1.88:8888/foo/login.jsp");
								//������ݵĲ��������Ƚ϶�Ļ����ԶԴ��ݵĲ������з�װ
								List<NameValuePair> params = new ArrayList<NameValuePair>();
								params.add(new BasicNameValuePair("name", name));
								params.add(new BasicNameValuePair("password", password));
								
								post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); //�����������
								HttpResponse response = httpClient.execute(post); //����post����
								
								if (response.getStatusLine().getStatusCode() == 200)//����������ɹ��ط�����Ӧ
								{
									String msg = EntityUtils.toString(response.getEntity());
									Looper.prepare();
									
									Toast.makeText(HttpClientTest.this, msg, Toast.LENGTH_LONG).show();//��ʾ��¼�ɹ�
									Looper.loop();
								}
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}.start();
				}
			}).setNegativeButton("ȡ��", null).show();
	}

}
