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
 * 使用HttpClient发送请求、接收响应很简单，只要如下几步：
 * 1、创建HttpClient对象。
 * 2、如果需要发送Get请求，创建HttpGet对象；如果需要发送Post请求，创建HttpPost对象。
 * 3、如果需要发送请求参数，可调用HttpGet、HttpPost共同的setParams(HttpParams params)方法来添加请求参数；
 *    对于HttpPost对象而言，也可调用setEntity(HttpEntity entity)方法来设置请求参数。
 * 4、调用HttpClient对象的execute(HttpUriRequest request)发送请求，执行该方法返回一个HttpResponse。
 * 5、调用HttpResponse的getAllHeaders()、getHeaders(String name)等方法可获取服务器的响应头；
 *    调用HttpResponse的getEntity()方法可获取HttpEntity对象，该对象包装了服务器的响应内容。
 *    程序可通过该对象获取服务器的响应内容。
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
				response.append(msg.obj.toString() + "\n");//使用response文本框显示服务器响应
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        httpClient = new DefaultHttpClient(); //创建DefaultHttpClient对象
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
				HttpGet get = new HttpGet("http://192.168.1.88:8888/foo/secret.jsp");//创建一个HttpGet对象
				try 
				{
					HttpResponse httpResponse = httpClient.execute(get); //发送get请求
					HttpEntity entity = httpResponse.getEntity();
					
					if (entity != null)
					{
						//读取服务器响应
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
		final View loginDialog = getLayoutInflater().inflate(R.layout.login, null);//加载登录界面
		
		//使用对话框供用户登录系统
		new AlertDialog.Builder(HttpClientTest.this)
			.setTitle("登录系统")
			.setView(loginDialog)
			.setPositiveButton("登录", new DialogInterface.OnClickListener() 
			{	
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					//获取用户输入的用户名、密码
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
								//如果传递的参数个数比较多的话可以对传递的参数进行封装
								List<NameValuePair> params = new ArrayList<NameValuePair>();
								params.add(new BasicNameValuePair("name", name));
								params.add(new BasicNameValuePair("password", password));
								
								post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); //设置请求参数
								HttpResponse response = httpClient.execute(post); //发送post请求
								
								if (response.getStatusLine().getStatusCode() == 200)//如果服务器成功地返回响应
								{
									String msg = EntityUtils.toString(response.getEntity());
									Looper.prepare();
									
									Toast.makeText(HttpClientTest.this, msg, Toast.LENGTH_LONG).show();//提示登录成功
									Looper.loop();
								}
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}.start();
				}
			}).setNegativeButton("取消", null).show();
	}

}
