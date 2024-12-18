package com.example.myapplication

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.Contact
// ... 其他导入

class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onDeleteClick: (Contact) -> Unit,
    private val onEditClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit,
    private val onSmsClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    // ViewHolder 类定义
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnCall: ImageButton = view.findViewById(R.id.btnCall)
        val btnSms: ImageButton = view.findViewById(R.id.btnSms)
    }

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    // 绑定数据 ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phoneNumber
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(contact)
        }
        
        holder.btnEdit.setOnClickListener {
            onEditClick(contact)
        }

        holder.btnCall.setOnClickListener {
            onCallClick(contact)
        }

        holder.btnSms.setOnClickListener {
            onSmsClick(contact)
        }
    }

    // 返回列表项数量
    override fun getItemCount() = contacts.size

    // 删除联系人的辅助方法
    fun removeContact(contact: Contact) {
        val position = contacts.indexOf(contact)
        if (position != -1) {
            contacts.removeAt(position)
            notifyItemRemoved(position)
        }
    }
} 