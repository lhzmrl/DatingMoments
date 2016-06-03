package com.kylin.datingmoments.activity;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kylin.datingmoments.R;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.DMUser;
import com.kylin.datingmoments.util.MD5Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kylin.datingmoments.R.id.com_actionbar_tv_title;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEtNickName;
    private EditText mEtEmail;
    private EditText mEtPwd;
    private EditText mEtPwd2;
    private View mTable;
    private ProgressBar mProgressBar;

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        TextView tv = (TextView) findViewById(R.id.com_actionbar_tv_title);
        tv.setText(R.string.register);
        mEtNickName = (EditText) findViewById(R.id.act_register_et_nick_name);
        mEtEmail = (EditText) findViewById(R.id.act_register_et_email);
        mEtPwd = (EditText) findViewById(R.id.act_register_et_pwd);
        mEtPwd2 = (EditText) findViewById(R.id.act_register_et_pwd2);
        mTable  = findViewById(R.id.act_register_table);
        mProgressBar = (ProgressBar) findViewById(R.id.act_register_pb);
        findViewById(R.id.act_register_btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtNickName.setError(null);
                mEtEmail.setError(null);
                mEtPwd.setError(null);
                mEtPwd2.setError(null);
                String email = mEtEmail.getText().toString();
                if (!isPasswordValid(email)){
                    mEtEmail.setError(getResources().getString(R.string.error_invalid_email));
                    return;
                }
                String nickname = mEtNickName.getText().toString();
                if ("".equals(nickname)){
                    mEtNickName.setError(getResources().getString(R.string.error_empty_nick_name));
                    return;
                }
                String pwd = mEtPwd.getText().toString();
                String pwd2 = mEtPwd2.getText().toString();
                if (!pwd.equals(pwd2) && !pwd.equals("")){
                    mEtPwd.setError(getResources().getString(R.string.error_not_consistent_password));
                    return;
                }
                DMUser user = new DMUser();
                user.setEmail(email);
                user.setNickName(nickname);
                user.setPwd(MD5Util.encryptToMd5(pwd));
                new RegisterAsyncTask().execute(user);
            }
        });
    }

    /**
     * 验证密码是否合法
     *
     * @param password 密码
     * @return true 合法，false 不合法
     */
    private boolean isPasswordValid(String password) {
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(password);
        boolean isMatched = matcher.matches();
        return isMatched;
    }

    private class RegisterAsyncTask extends AsyncTask<DMUser,Void,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTable.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(DMUser... params) {
            if (mDAO.isUserInfoExist(params[0])){
                return 0;
            }
            DMUser user = mDAO.register(params[0]);
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mTable.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            switch (result){
                case 0:
                    mEtNickName.setError(getResources().getString(R.string.nick_name_has_exist));
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(),"注册成功！",Toast.LENGTH_LONG).show();
                    finish();
            }
        }
    }
}
