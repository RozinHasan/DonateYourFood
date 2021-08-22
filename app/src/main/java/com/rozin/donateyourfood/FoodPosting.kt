package com.rozin.donateyourfood

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.format.Time
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.parse.*
import com.rozin.donateyourfood.models.FirebaseResponse
import com.rozin.donateyourfood.models.PushRawModel
import com.rozin.donateyourfood.models.rawPushModel
import com.rozin.donateyourfood.networks.ApiService
import com.rozin.donateyourfood.networks.RetrofitBuilder
import com.rozin.donateyourfood.utils.MyPreference
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class FoodPosting : AppCompatActivity() {
    lateinit var mdrname: EditText
    lateinit var mdraddress: EditText
    lateinit var mdrzipcode: EditText
    lateinit var mdrphone: EditText
    lateinit var mdrnumpeople: EditText
    lateinit var mdrsubmitpost: Button
    lateinit var mradiodr: RadioGroup
    lateinit var mdatetext: TextView

    protected var mTokens: List<ParseObject> = ArrayList()
    private var allTokens:String? = null
    private var tokenList:ArrayList<String> = ArrayList()
    private var myRatings:Int = 0
    private var distSpinner:Spinner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)

        setContentView(R.layout.activity_food_posting)

        mdatetext = findViewById<View>(R.id.datetext) as TextView

        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())

        mdatetext.text = currentDateTimeString

        mradiodr = findViewById<View>(R.id.radioGroupDR) as RadioGroup
        mdrname = findViewById<View>(R.id.DRname) as EditText
        mdraddress = findViewById<View>(R.id.DRaddress) as EditText
        mdrzipcode = findViewById<View>(R.id.DRzipcode) as EditText
        mdrphone = findViewById<View>(R.id.DRphone) as EditText
        mdrnumpeople = findViewById<View>(R.id.peoplenum) as EditText
        mdrsubmitpost = findViewById<View>(R.id.post_button) as Button
        distSpinner = findViewById<View>(R.id.spinner) as Spinner

        mdrsubmitpost.setOnClickListener {
            var drname = mdrname.text.toString()
            var draddress = mdraddress.text.toString()
            var drzipcode = mdrzipcode.text.toString()
            var drphone = mdrphone.text.toString()
            var drnumpeople = mdrnumpeople.text.toString()
            val DorR: String?
            val fulldetail: String
            val postdate = mdatetext.text.toString()
            drname = drname.trim { it <= ' ' }
            draddress = draddress.trim { it <= ' ' }
            drzipcode = drzipcode.trim { it <= ' ' }
            drphone = drphone.trim { it <= ' ' }
            drnumpeople = drnumpeople.trim { it <= ' ' }

            if (mradiodr.checkedRadioButtonId == R.id.radioDonor) {
                DorR = "donor"

            } else if (mradiodr.checkedRadioButtonId == R.id.radioRecipient) {
                DorR = "recipient"

            } else {
                DorR = null
            }



            if (drname.isEmpty() || draddress.isEmpty() || drzipcode.isEmpty() ||
                    drphone.isEmpty() || drnumpeople.isEmpty() || DorR == null ||
                    drzipcode.isEmpty() || drphone.isEmpty()) {
                //                    AlertDialog.Builder builder = new AlertDialog.Builder(FoodPosting.this);
                //                    builder.setBody(R.string.submit_error_message)
                //                            .setTitle(R.string.signup_error_title)
                //                            .setPositiveButton(android.R.string.ok, null);
                //                    AlertDialog dialog = builder.create();
                //                    dialog.show();
            } else {

                val progressDialog = ProgressDialog(this);
                progressDialog.setMessage("Posting your data to the server..."); // Setting Message
                progressDialog.setTitle("Please wait"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);

                //submit
                fulldetail = drname + "\n" + draddress + "\n" + drzipcode + "\n" + "Phone: " + drphone + "\n" + "Approximate People: " + drnumpeople + "\n" + "Post Time: " + postdate
                Log.d("fulldetail", fulldetail)
                val postingdata = ParseObject("foodtable")
                postingdata.put("name", drname) //string
                postingdata.put("address", draddress) //integer
                postingdata.put("zipcode", drzipcode) //variable
                postingdata.put("phone", drphone)
                postingdata.put("numofpeople", drnumpeople)
                postingdata.put("dorR", DorR)
                postingdata.put("full", fulldetail)
                postingdata.put("status", "0")
                postingdata.put("postdate", postdate)
                postingdata.put("uid", ParseUser.getCurrentUser().objectId)
                postingdata.put("uName", ParseUser.getCurrentUser().username)
                postingdata.put("ratings", 0)
                postingdata.put("city", distSpinner?.selectedItem.toString())
                setProgressBarIndeterminateVisibility(true)
                postingdata.saveInBackground { e ->
                    setProgressBarIndeterminateVisibility(false)

                    if (e == null) {

                        val json = PushRawModel()
                        json.deviceToken = tokenList
                        json.image = "https://cdn1.imggmi.com/uploads/2019/6/21/b1961c1f2db203f96c7a4e637010da48-full.png"
                        json.lat = "0.0"
                        json.lng = "0.0"
                        json.body = fulldetail
                        json.uid = ""

                        sendPushToAll(json)
                        progressDialog.dismiss();
                        showSucessDialog()



                    } else {
                        showErrorDialog(e)
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        myRatings = MyPreference.with(this).getInt("user_ratings", 0)
        getAllUniqueTokens()

    }

    private fun generateUid():String {
        val time = Time()
        time.setToNow()
        return  java.lang.Long.toString(time.toMillis(false))
    }

    private fun showErrorDialog(e:ParseException){
        val builder2 = AlertDialog.Builder(this@FoodPosting)
        builder2.setMessage(e.message)
                .setTitle(R.string.signup_error_title)
                .setPositiveButton(android.R.string.ok, null)
        val dialog2 = builder2.create()
        dialog2.show()
    }

    private fun showSucessDialog(){
        // Success!
        val builder1 = AlertDialog.Builder(this@FoodPosting)
        builder1.setMessage(R.string.submit_success)
                .setTitle(R.string.submit_success_title)
                .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        val intent = Intent(this@FoodPosting, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish()
                    }

                })
        val dialog1 = builder1.create()
        dialog1.show()
        //mdrname.setText("",null);
    }

    fun notifyAllCloudUsers( title: String, message:String ) {
        val json = rawPushModel()
//        json.email = "test@gmail.com"    //this field is needed when we want to send push to a specific single device which result will be queried by email
        json.title = title
        json.message = message
        json.iconUrl = "https://imgur.com/wZFGvTV"
//        json.iconUrl = "https://imgur.com/wZFGvTV"

        val service = RetrofitBuilder.buildRetrofit().create(ApiService::class.java)

        val call = service.sendMultiplePush(json)
        call.enqueue(object : Callback<FirebaseResponse> {
            @SuppressLint("LongLogTag")
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onResponse(call: Call<FirebaseResponse>, response: Response<FirebaseResponse>) {
                Log.e("pushResponse", "pushToAllResponse --> $response")
                //progressD.dismiss()

                if (response.code() == 200) {
                    Log.e("Firebase msg response :",  GsonBuilder().setPrettyPrinting().create().toJson(response))
                    val dataList = response.body()
                    if (dataList != null) {
                        if (dataList.success == 1L){
                            Log.e("Notify to all user sucess msg id =:", dataList.multicastId.toString())
                        }
                    }

                } else {
//                    Toast.makeText(context, "failed to load Welcome datas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FirebaseResponse>, t: Throwable?) {
                Log.e("pushApiFailure :", t?.message)
            }
        })

    }

    private fun sendPushToAll(rawJson : PushRawModel){
        val service = RetrofitBuilder.buildRetrofit().create(ApiService::class.java)

        val call = service.sendPushNotification(rawJson)
        call.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("LongLogTag")
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.e("pushResponse", "pushToAllResponse --> $response")
                //progressD.dismiss()

                if (response.code() == 200) {
                    Log.e("Firebase msg response :",  GsonBuilder().setPrettyPrinting().create().toJson(response))
                    val dataList = response.body()
                    if (dataList != null) {
                            Log.e("Notify to all user sucess =:", "")
                    }

                } else {
//                  Toast.makeText(context, "failed to load Welcome datas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable?) {
                Log.e("pushApiFailure :", t?.message)
            }
        })

    }

    private fun getAllUniqueTokens():ArrayList<String>{
        tokenList.clear()

        val builder = StringBuilder()
        val query = ParseQuery.getQuery<ParseObject>("UserToken")
        query.whereEqualTo("getTokens", "tokens")
        query.findInBackground { tokens, e ->
            if (e == null) {
                mTokens = tokens


                val mtokens = arrayOfNulls<String>(mTokens.size)
                val mytoken = MyPreference.with(this).getString("myToken", "")

                var i = 0
                for (postingdata in mTokens) {
                    mtokens[i] = postingdata.getString("token")
                    builder.append(mtokens[i]).append(",")
                    Log.e("token"+ i.toString(), mtokens[i])

                    if (!tokenList.contains(mtokens[i])){
                        if (mtokens[i] != mytoken){
                            tokenList.add(mtokens[i]!!)
                        }
                    }


                    i++
                }
                Log.e("Token size", tokenList.size.toString())

                allTokens = builder.deleteCharAt(builder.length - 1).toString()
                Log.e("tokens", allTokens)


            } else {
                Log.e("", e.message)
            }
        }



        return tokenList

    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.food_posting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val itemId = item.itemId

        if (itemId == R.id.action_logout) {
            ParseUser.logOut()
            navigateToLogin()
        } else if (itemId == R.id.action_posting) {
            val intent = Intent(this, FoodPosting::class.java)
            startActivity(intent)
        } else if (itemId == R.id.action_home) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        } else if (itemId == R.id.action_about) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
