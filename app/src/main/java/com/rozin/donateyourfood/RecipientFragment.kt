package com.rozin.donateyourfood

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment

import androidx.fragment.app.ListFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.rozin.donateyourfood.adapter.DonorAdapter
import com.rozin.donateyourfood.adapter.RecipientAdapter
import com.rozin.donateyourfood.models.ItemModel
import com.rozin.donateyourfood.utils.MyPreference

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class RecipientFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    lateinit var mRecipients: List<ParseObject>
    internal lateinit var recyclerView: RecyclerView
    private var adapter: RecipientAdapter? = null
    private var recipientList: ArrayList<ItemModel> = ArrayList()
    private var tvRNotfound :TextView? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recipient, container, false)

        tvRNotfound = view.findViewById<TextView>(R.id.tv_noRfound)
        recyclerView = view.findViewById<RecyclerView>(R.id.recipient_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        refreshLayout = view.findViewById(R.id.reciepent_layout)
        refreshLayout!!.setOnRefreshListener(this)
        refreshLayout!!.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)

        return view

    }

    override fun onRefresh() {
        refreshLayout!!.isRefreshing = true
        loadReciepent()
    }


    override fun onResume() {
        super.onResume()
        loadReciepent()


    }

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    private fun loadReciepent(){
        if (isLoading) {
            return
        }
        isLoading = true
        val city = MyPreference.with(context).getString("city", "")


        val query = ParseQuery.getQuery<ParseObject>("foodtable")
        query.whereEqualTo("dorR", "recipient")
        query.whereEqualTo("city", city)
        query.findInBackground { posts, e ->

            if (e == null) {
                mRecipients = posts

                val recipients = arrayOfNulls<String>(mRecipients.size)
                var i = 0

                val dateFormat = SimpleDateFormat("MMM dd,yyyy hh:mm:ss aa")//("MM/dd/yyyy hh:mm:ss aa");
                var convertedDatepost = Date()
                var convertedDatecurrent = Date()
                val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
                try {
                    convertedDatecurrent = dateFormat.parse(currentDateTimeString)
                } catch (e1: java.text.ParseException) {
                    e1.printStackTrace()
                }

                Log.d(TAG, "todays date=$currentDateTimeString")

                for (postingdata in mRecipients) {
                    val dt = postingdata.getString("postdate")
                    try {
                        convertedDatepost = dateFormat.parse(dt)
                    } catch (e1: java.text.ParseException) {
                        e1.printStackTrace()
                    }

                    Log.d(TAG, "postdate=" + dt!!)


                    val diff = convertedDatecurrent.time - convertedDatepost.time
                    Log.d(TAG, "datediff=$diff")
                    val seconds = diff / 1000
                    Log.d(TAG, "seconddiff=$seconds")

                    val minutes = seconds / 60
                    Log.d(TAG, "minutediff=$minutes")

                    val hours = minutes / 60
                    Log.d(TAG, "hourdiff=$hours")

                    if (hours >= 24) {
                        postingdata.deleteInBackground()
                    }

                }






                recipientList.clear()
                for (postingdata in mRecipients) {

//                    recipients[i] = postingdata.getString("full")

                    val itemModel = ItemModel()
                    itemModel.organization = postingdata.getString("name")
                    itemModel.address = postingdata.getString("address")
                    itemModel.phone = postingdata.getString("phone")
                    itemModel.peoplAapprox = postingdata.getString("numofpeople")
                    itemModel.zipcode = postingdata.getString("zipcode")
                    itemModel.postTime = postingdata.getString("postdate")
                    itemModel.status = postingdata.getString("status")
                    itemModel.uid = postingdata.getString("uid")
                    itemModel.userName = postingdata.getString("uName")
                    itemModel.acceptedId = postingdata.getString("acceptedId")
                    itemModel.objectId = postingdata.objectId
                    itemModel.rating = postingdata.getInt("ratings")
                    itemModel.city = postingdata.getString("city")

                    recipientList.add(itemModel)

                    i++


                }


                if (recipientList.size > 0){
                    tvRNotfound?.visibility = View.GONE

                    adapter = RecipientAdapter(recipientList, context!!)
                    recyclerView.adapter = adapter
                    adapter!!.notifyDataSetChanged()

                }else{
                    tvRNotfound?.visibility = View.VISIBLE
                }


                isLoading = false
                refreshLayout!!.isRefreshing = false


            } else {

                isLoading = false
                refreshLayout!!.isRefreshing = false

                e.message?.let { Log.e(TAG, it) }
                val builder = AlertDialog.Builder(context)
                builder.setMessage(e.message)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    companion object {
        val TAG = RecipientFragment::class.java.simpleName
    }


}
