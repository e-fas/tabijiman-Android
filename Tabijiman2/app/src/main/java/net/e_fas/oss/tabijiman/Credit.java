//  Copyright (c) 2016 FUKUI Association of information & system industry. All rights reserved.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

package net.e_fas.oss.tabijiman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Credit extends AppCompatActivity {

    static TextView odp;

    private View getActionBarView() {

        // 表示するlayoutファイルの取得
        LayoutInflater inflater = LayoutInflater.from(this);
        return inflater.inflate(R.layout.credit_bar, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credit);

        odp = (TextView) findViewById(R.id.LabelOdp);

        odp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://developer.odp.jig.jp/data/#assetlist");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });

        // ActionBarの設定
        if (savedInstanceState == null) {

            // customActionBarの取得
            View customActionBarView = this.getActionBarView();
            // ActionBarの取得
            ActionBar actionBar = this.getSupportActionBar();

            if (actionBar != null) {
                // タイトルを表示するか（もちろん表示しない）
                actionBar.setDisplayShowTitleEnabled(false);
                // iconを表示するか（もちろん表示しない）
                actionBar.setDisplayShowHomeEnabled(false);
                // ActionBarにcustomViewを設定する
                actionBar.setCustomView(customActionBarView);
                // CutomViewを表示するか
                actionBar.setDisplayShowCustomEnabled(true);
            }
        }
    }
}
