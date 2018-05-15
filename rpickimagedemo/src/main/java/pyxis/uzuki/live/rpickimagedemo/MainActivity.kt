package pyxis.uzuki.live.rpickimagedemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import pyxis.uzuki.live.richutilskt.module.image.OrientationFixer
import pyxis.uzuki.live.richutilskt.utils.RPickMedia

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelect.setOnClickListener {
            RPickMedia.instance.pickFromGallery(this, { code, path ->
                val fixedPath = OrientationFixer.execute(path, this)
                txtPath.text = "code: $code, path: $fixedPath"
                Glide.with(this).load(path).into(imgPicture)
            })
        }

        btnTest.setOnClickListener {
            startActivity(Intent(this, JavaActivity::class.java))
        }
    }
}