package pyxis.uzuki.live.richutils.samplejava;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.json.JSONObject;

import butterknife.BindView;
import pyxis.uzuki.live.richutilskt.utils.RichUtils;

/**
 * Created by pyxis on 2017. 6. 12..
 */

public class JSONActivity extends BaseActivity {
    @BindView(R.id.txtResult) TextView txtResult;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        String jsonStr = RichUtils.fileAsString(this.getAssets(), "json", "customer.json");
        JSONObject jsonObject = RichUtils.createJSONObject(jsonStr);

        if (jsonObject == null) {
            txtResult.setText("Json file isn't given!");
            return;
        }


        String firstName = RichUtils.getJSONString(jsonObject, "firstName");
        String lastName = RichUtils.getJSONString(jsonObject, "lastName");
        int age = RichUtils.getJSONInt(jsonObject, "age");



    }
}
