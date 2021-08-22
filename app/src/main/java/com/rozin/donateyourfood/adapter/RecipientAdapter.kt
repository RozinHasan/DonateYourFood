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
import java.util.ArrayList

class RecipientAdapter(mItemList: ArrayList<ItemModel>, private val context: Context) : RecyclerView.Adapter<RecipientAdapter.ViewHolder>() {
    private var mItemList = ArrayList<ItemModel>()

    init {
        this.mItemList = mItemList
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): RecipientAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reciepent, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RecipientAdapter.ViewHolder, position: Int) {

        val model = mItemList[position]
        holder.recipient_organization.text = model.organization
        holder.recipient_address.text = "Address :" +model.address
        holder.recipient_zip.text ="Zip code : "+ model.zipcode
        holder.recipient_phone.text = "Phone :"+ model.phone
        holder.recipient_peopleApprox.text ="Approximate people : "+ model.peoplAapprox
        holder.recipient_postingTime.text = "Post time : "+model.postTime
        holder.ratingBar.rating = model.rating.toFloat()
        holder.tv_uname.text = model.userName

        if (model.status == "0"){

                holder.btnRAccept.isEnabled = true
                holder.btnRAccept.text = "Accept"
//            holder.btnRAccept.setBackgroundColor(android.R.color.holo_green_light)

        }else if (model.status == "1"){
            holder.btnRAccept.isEnabled = false
            holder.btnRAccept.text = "Accepted"
//            holder.btnRAccept.setBackgroundColor(android.R.color.holo_red_light)

            if (model.acceptedId != null){
                if (model.acceptedId.equals(ParseUser.getCurrentUser().objectId)){
                    holder.btnMessages.visibility = View.VISIBLE
                }
            }
        }

        if (model?.uid.equals(ParseUser.getCurrentUser()?.objectId)){
//            holder.btnMessages.visibility == View.VISIBLE
            holder.btnRAccept.visibility = View.INVISIBLE
            holder.btnIncomingMsg.visibility = View.VISIBLE
        }

        holder.btnRAccept?.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val query = ParseQuery.getQuery<ParseObject>("foodtable")
                query.whereEqualTo("objectId", model.objectId)
                query.findInBackground { posts, e ->

                    if (e == null) {
                        var i = 0
                        var status = posts.get(0).getString("status")
                        if (status == "1"){
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Already accepted by someone, press ok to refresh!")
                                    .setTitle("Opps!")
                                    .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            dialog?.dismiss()
                                            val intent = Intent(context, Home::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            context.startActivity(intent);
                                            (context as AppCompatActivity).finish()
                                        }

                                    })
                                    .setCancelable(false)
                            val dialog = builder.create()
                            dialog.show()
                        }else if (status == "0"){

                            val progressDialog = ProgressDialog(context);
                            progressDialog.setMessage("Please wait while accepting the request..."); // Setting Message
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                            progressDialog.show(); // Display Progress Dialog
                            progressDialog.setCancelable(false);

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
                        Log.e(DonorFragment.TAG, e.message)
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


        holder.btnIncomingMsg.setOnClickListener {
            if (model.acceptedId != null){
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("SenderId", ParseUser.getCurrentUser().objectId)
                intent.putExtra("RecieverId", model.acceptedId)
                intent.putExtra("postObjectId", model.objectId)
                context.startActivity(intent);
            }else{
                Snackbar.make(it.rootView, R.string.not_accepted, Snackbar.LENGTH_SHORT).show()
            }
        }


        holder.btnMessages.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("SenderId", ParseUser.getCurrentUser().objectId)
            intent.putExtra("RecieverId", model.uid)
            intent.putExtra("postObjectId", model.objectId)
            context.startActivity(intent);
        }
    }

    private fun showSucessDialog(){
        // Success!
        val builder1 = AlertDialog.Builder(context)
        builder1.setMessage("You have successfully accepted the request")
                .setTitle("Done!")
                .setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        val intent = Intent(context, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        (context as AppCompatActivity).finish()
                    }

                })
                .setCancelable(false)
        val dialog1 = builder1.create()
        dialog1.show()
        //mdrname.setText("",null);
    }


    private fun showErrorDialog(e: ParseException){
        val builder2 = AlertDialog.Builder(context)
        builder2.setMessage(e.message)
                .setTitle(R.string.signup_error_title)
                .setPositiveButton(android.R.string.ok, null)
        val dialog2 = builder2.create()
        dialog2.show()
    }
    override fun getItemCount(): Int {
        return mItemList?.size ?: 0
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recipient_organization: TextView
        var recipient_address: TextView
        var recipient_zip: TextView
        var recipient_phone: TextView
        var recipient_peopleApprox: TextView
        var recipient_postingTime: TextView
        var recipient_postedBy: TextView
        var btnRAccept: Button
        var btnMessages: Button
        var btnIncomingMsg: Button
        var ratingBar:MaterialRatingBar
        var tv_uname:TextView

        init {

            recipient_organization = itemView.findViewById(R.id.recipient_organization_name)
            recipient_address = itemView.findViewById(R.id.recipient_address)
            recipient_zip = itemView.findViewById(R.id.recipient_zipcode)
            recipient_phone = itemView.findViewById(R.id.recipient_phone)
            recipient_peopleApprox = itemView.findViewById(R.id.recipient_people_approx)
            recipient_postingTime = itemView.findViewById(R.id.recipient_posting_time)
            recipient_postedBy = itemView.findViewById(R.id.recipient_accpetStatus)
            btnRAccept = itemView.findViewById(R.id.recipient_accpetBtn)
            btnMessages = itemView.findViewById(R.id.recipient_accpetStatus)
            btnIncomingMsg = itemView.findViewById(R.id.recipient_incomingMsg)
            ratingBar = itemView.findViewById(R.id.reciever_ratingBar)
            tv_uname = itemView.findViewById(R.id.reciever_postedBy)
        }
    }
}
