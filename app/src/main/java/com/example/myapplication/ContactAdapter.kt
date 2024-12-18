package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

class ContactAdapter(
    context: Context,
    private val contacts: List<Contact>,
    private val onCallClick: (Contact) -> Unit,
    private val onMessageClick: (Contact) -> Unit
) : ArrayAdapter<Contact>(context, 0, contacts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.contact_list_item, parent, false)

        val contact = contacts[position]

        view.findViewById<TextView>(R.id.nameTextView).text = contact.name
        view.findViewById<TextView>(R.id.phoneTextView).text = contact.phoneNumber

        view.findViewById<ImageButton>(R.id.callButton).setOnClickListener {
            onCallClick(contact)
        }
        view.findViewById<ImageButton>(R.id.messageButton).setOnClickListener {
            onMessageClick(contact)
        }

        return view
    }
} 