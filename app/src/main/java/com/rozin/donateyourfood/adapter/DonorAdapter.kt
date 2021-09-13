package com.rozin.donateyourfood.adapter


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.rozin.donateyourfood.ChatActivity
import com.rozin.donateyourfood.DonorFragment
import com.rozin.donateyourfood.Home
import com.rozin.donateyourfood.R
import com.rozin.donateyourfood.models.ItemModel
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.util.*


class DonorAdapter(mItemList: ArrayList<ItemModel>, private val context: Context) : RecyclerView.Adapter<DonorAdapter.ViewHolder>() {
    private var mItemList = ArrayList<ItemModel>()

    init {
        this.mItemList = mItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): DonorAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_donor, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: DonorAdapter.ViewHolder, position: Int) {

        val model = mItemList[position]
        holder.donor_organization.text = model.organization
        holder.donor_address.text = "Address : " + model.address
        holder.donor_zip.text = "Zip code : " + model.zipcode
        holder.donor_phone.text = "Phone :" + model.phone
        holder.donor_peopleApprox.text = "Approximate people : " + model.peoplAapprox
        holder.donor_postingTime.text = "Post time : " + model.postTime
        holder.ratingBar.rating = model.rating.toFloat()
        holder.tv_uname.text = model.userName

        if (model.status == "0") {

            holder.btnDAccept.isEnabled = true
            holder.btnDAccept.text = "Accept"
//            holder.btnDAccept.setBackgroundColor(android.R.color.holo_green_light)


        } else if (model.status == "1") {
            holder.btnDAccept.isEnabled = false
            holder.btnDAccept.text = "Accepted"
//            holder.btnDAccept.setBackgroundColor(android.R.color.holo_red_light)
            if (model.acceptedId != null) {
                if (model.acceptedId.equals(ParseUser.getCurrentUser().objectId)) {
                    holder.donorMsgBtn.visibility = View.VISIBLE
                }
            }
        }

        if (model.uid.equals(ParseUser.getCurrentUser()?.objectId)) {
//            holder.btnMessages.visibility == View.VISIBLE
            holder.btnDAccept.visibility = View.INVISIBLE
            holder.btnIncomingMsg.visibility = View.VISIBLE
        }

        holder.btnDAccept.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val query = ParseQuery.getQuery<ParseObject>("foodtable")
                query.whereEqualTo("objectId", model.objectId)
                query.findInBackground { posts, e ->

                    if (e == null) {
                        var i = 0
                        var status = posts.get(0).getString("status")
                        if (status == "1") {
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Already accepted by someone, press ok to refresh!")
                                    .setTitle("Opps!")
                                    .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            dialog?.dismiss()
                                            val intent = Intent(context, Home::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            context.startActivity(intent)
                                            (context as AppCompatActivity).finish()
                                        }

                                    })
                                    .setCancelable(false)
                            val dialog = builder.create()
                            dialog.show()
                        } else if (status == "0") {

                            val progressDialog = ProgressDialog(context)
                            progressDialog.setMessage("Please wait while accepting the request...") // Setting Message
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // Progress Dialog Style Spinner
                            progressDialog.show() // Display Progress Dialog
                            progressDialog.setCancelable(false)

                            posts.get(0).put("status", "1")
                            posts.get(0).put("acceptedId", ParseUser.getCurrentUser().objectId)
                            posts.get(0).saveInBackground { e ->


                                if (e == null) {
                                    showSucessDialog()
                                    progressDialog.dismiss()
                                } else {
                                    showErrorDialog(e)
                                }
                            }

                        }


                    } else {
                        e.message?.let { Log.e(DonorFragment.TAG, it) }
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage(e.message)
                                .setTitle(R.string.login_error_title)
                                .setPositiveButton(android.R.string.ok, null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            }

        })




        holder.donorMsgBtn.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("SenderId", ParseUser.getCurrentUser().objectId)
            intent.putExtra("RecieverId", model.uid)
            intent.putExtra("recieverName", model.userName)
            intent.putExtra("postObjectId", model.objectId)
            context.startActivity(intent);
        }

        holder.btnIncomingMsg.setOnClickListener {
            if (model.acceptedId != null) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("SenderId", ParseUser.getCurrentUser().objectId)
                intent.putExtra("RecieverId", model.acceptedId)
                intent.putExtra("recieverName", model.userName)
                intent.putExtra("postObjectId", model.objectId)
                context.startActivity(intent);
            } else {
                Snackbar.make(it.rootView, R.string.not_accepted, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSucessDialog() {
        // Success!
        val builder1 = AlertDialog.Builder(context)
        builder1.setMessage("You have successfully accepted the request")
                .setTitle("Done!")
                .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        val intent = Intent(context, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        (context as AppCompatActivity).finish()
                    }

                })
                .setCancelable(false)
        val dialog1 = builder1.create()
        dialog1.show()
        //mdrname.setText("",null);
    }


    private fun showErrorDialog(e: ParseException) {
        val builder2 = AlertDialog.Builder(context)
        builder2.setMessage(e.message)
                .setTitle(R.string.signup_error_title)
                .setPositiveButton(android.R.string.ok, null)
        val dialog2 = builder2.create()
        dialog2.show()
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var donor_organization: TextView
        var donor_address: TextView
        var donor_zip: TextView
        var donor_phone: TextView
        var donor_peopleApprox: TextView
        var donor_postingTime: TextView
        var donor_postedBy: TextView
        var donorMsgBtn: Button
        var btnDAccept: Button
        var btnIncomingMsg: Button
        var ratingBar: MaterialRatingBar
        var tv_uname:TextView

        init {

            donor_organization = itemView.findViewById(R.id.donor_organization_name)
            donor_address = itemView.findViewById(R.id.donor_address)
            donor_zip = itemView.findViewById(R.id.donor_zipcode)
            donor_phone = itemView.findViewById(R.id.donor_phone)
            donor_peopleApprox = itemView.findViewById(R.id.donor_people_approx)
            donor_postingTime = itemView.findViewById(R.id.donor_posting_time)
            donor_postedBy = itemView.findViewById(R.id.donor_accpetStatus)
            donorMsgBtn = itemView.findViewById(R.id.donor_accpetStatus)
            btnDAccept = itemView.findViewById(R.id.donor_accpetBtn)
            btnIncomingMsg = itemView.findViewById(R.id.donor_showIncomingMsg)
            ratingBar = itemView.findViewById(R.id.donor_ratingBar)
            tv_uname = itemView.findViewById(R.id.donor_postedBy)
        }
    }
}
