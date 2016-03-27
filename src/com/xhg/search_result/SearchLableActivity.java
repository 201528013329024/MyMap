package com.xhg.search_result;

import com.baidu.location.i;
import com.xhg.mymap.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SearchLableActivity extends Activity {
	public EditText search_bar = null;
	public Button[] search_label = new Button[44];
	public int[] btn_ID = new int[] { R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
			R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10, R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15,
			R.id.btn16, R.id.btn17, R.id.btn18, R.id.btn19, R.id.btn20, R.id.btn21, R.id.btn22, R.id.btn23, R.id.btn24,
			R.id.btn25, R.id.btn26, R.id.btn27, R.id.btn28, R.id.btn29, R.id.btn30, R.id.btn31, R.id.btn32, R.id.btn33,
			R.id.btn34, R.id.btn35, R.id.btn36, R.id.btn37, R.id.btn38, R.id.btn39, R.id.btn40, R.id.btn41, R.id.btn42,
			R.id.btn43 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_view);

		ImageView img_OK = (ImageView) findViewById(R.id.img_OK);
		search_bar = (EditText) findViewById(R.id.search_bar);

		img_OK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (search_bar.getText().toString() != "") {
					Intent in = new Intent();
					in.putExtra("result", search_bar.getText().toString());
					setResult(RESULT_OK, in);
					finish();
				}
			}
		});

		
		for (int i = 0; i < 44; i++) {
			search_label[i] = (Button) findViewById(btn_ID[i]);
			search_label[i].setId(i);

			search_label[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent in = new Intent();
					in.putExtra("result", search_label[v.getId()].getText().toString());
					setResult(RESULT_OK, in);
					finish();
				}
			});
		}
	}
}
