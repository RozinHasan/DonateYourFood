package com.rozin.donateyourfood

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.parse.ParseUser
import com.rozin.donateyourfood.adapter.TabAdapter
import com.rozin.donateyourfood.utils.MyPreference
import java.lang.Exception
import java.lang.NullPointerException


class Home : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    private var adapter: TabAdapter? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var spinner: Spinner? = null
    val cityNames: ArrayList<String> = ArrayList()
    private var isFirstTime = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home)
        setSpinner()


        supportActionBar?.title = ParseUser.getCurrentUser().username
        supportActionBar?.subtitle = Html.fromHtml("<small>" + ParseUser.getCurrentUser().email + "</small>")


        val currentUser = ParseUser.getCurrentUser()
        if (currentUser == null) {
            navigateToLogin()
        } else {
            Log.i(TAG, currentUser.username)
        }

        viewPager = findViewById<ViewPager>(R.id.pager)
        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        adapter = TabAdapter(supportFragmentManager)
        adapter?.addFragment(DonorFragment(), "Donor List")
        adapter?.addFragment(RecipientFragment(), "Reciepent List")
        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)

        val tabIcons = intArrayOf(R.drawable.ic_donor, R.drawable.ic_volunteer)
        tabLayout?.getTabAt(0)!!.setIcon(tabIcons[0])
        tabLayout?.getTabAt(1)!!.setIcon(tabIcons[1])

    }

    private fun setSpinner() {
        val cities = arrayOf("Dhaka", "Chittagong", "Rajshahi", "Khulna", "Sylhet", "Mymensingh", "Barishal", "Rangpur", "Comilla", "Narayanganj", "Gazipur", "Gopalganj")

        cityNames.clear()
        for (c in cities) {
            cityNames.add(c)
        }

        spinner = findViewById(R.id.locSpinner)
        spinner?.setOnItemSelectedListener(this)


        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.setAdapter(aa)

        try {
            val city = MyPreference.with(this).getString("city", "Dhaka")
            val spinnerPosition = aa.getPosition(city)
            spinner?.setSelection(spinnerPosition)
        }catch (ex:NullPointerException){
            ex.printStackTrace()
        }



    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        MyPreference.with(this).addString("city", cityNames[position]).save()
        MyPreference.with(this).addInt("pos", position).save()

        if (isFirstTime){
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

        isFirstTime = true

    }


    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val itemId = item.itemId

        if (itemId == R.id.action_logout) {
            ParseUser.logOut()
            MyPreference.with(this).addBoolean("isTokenSaved", false).save()
            navigateToLogin()
        } else if (itemId == R.id.action_posting) {
            val intent = Intent(this, FoodPosting::class.java)
            startActivity(intent)
        } else if (itemId == R.id.action_home) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        } else if (itemId == R.id.action_about) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else if (itemId == R.id.action_refresh) {


        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        val TAG = Home::class.java.simpleName
    }

}
