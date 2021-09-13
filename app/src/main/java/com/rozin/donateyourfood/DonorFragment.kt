package com.rozin.donateyourfood

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.rozin.donateyourfood.adapter.DonorAdapter
import com.rozin.donateyourfood.models.ItemModel
import com.rozin.donateyourfood.utils.MyPreference

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class DonorFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    lateinit var mDonors: List<ParseObject>
    internal lateinit var recyclerView: RecyclerView
    private var adapter: DonorAdapter? = null
    private var donorList: ArrayList<ItemModel> = ArrayList()
    private var tvNotfound :TextView? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var isLoading = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_donor, container, false)

        tvNotfound = view.findViewById<TextView>(R.id.tv_noDfound)
        recyclerView = view.findViewById<RecyclerView>(R.id.donor_recycler) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)

        refreshLayout = view.findViewById(R.id.donor_layout)
        refreshLayout!!.setOnRefreshListener(this)
        refreshLayout!!.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)

        return view
    }

    override fun onRefresh() {
        refreshLayout!!.isRefreshing = true
        loadDonorList()
    }


    override fun onResume() {
        super.onResume()
        loadDonorList()
    }

    private fun loadDonorList(){
        if (isLoading) {
            return
        }
        isLoading = true
        val city = MyPreference.with(context).getString("city", "")

        val query = ParseQuery.getQuery<ParseObject>("foodtable")
        query.whereEqualTo("dorR", "donor")
        query.whereEqualTo("city", city)
        query.findInBackground { posts, e ->


            if (e == null) {

                mDonors = posts

                val donors = arrayOfNulls<String>(mDonors.size)
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

                for (postingdata in mDonors) {
                    val dt = postingdata.getString("postdate")
                    try {
                        convertedDatepost = dateFormat.parse(dt)
                    } catch (e1: java.text.ParseException) {
                        e1.printStackTrace()
                    }

                    Log.d(TAG, "postdate=" + dt!!)


                    val diff = convertedDatecurrent.time - convertedDatepost.time
                    Log.d(TAG, "datediff=" + java.lang.Long.toString(diff))
                    val seconds = diff / 1000
                    Log.d(TAG, "seconddiff=" + java.lang.Long.toString(seconds))

                    val minutes = seconds / 60
                    Log.d(TAG, "minutediff=" + java.lang.Long.toString(minutes))

                    val hours = minutes / 60
                    Log.d(TAG, "hourdiff=" + java.lang.Long.toString(hours))

                    if (hours >= 24) {
                        postingdata.deleteInBackground()
                    }

                }

                donorList.clear()
                for (postingdata in mDonors) {
//                    donors[i] = postingdata.getString("full")

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

                    donorList.add(itemModel)
                    i++

                }

                if (donorList.size > 0){
                    tvNotfound?.visibility = View.GONE

                    adapter = DonorAdapter(donorList, context!!)
                    recyclerView.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }else{
                    tvNotfound?.visibility = View.VISIBLE
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
        val TAG = DonorFragment::class.java.simpleName
    }
}

