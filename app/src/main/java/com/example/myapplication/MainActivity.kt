package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.ContactsAdapter
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager

class MainActivity : AppCompatActivity() {
    private lateinit var contactsManager: ContactsManager
    private lateinit var adapter: ContactsAdapter
    private val contacts = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactsManager = ContactsManager(this)
        
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = ContactsAdapter(
            contacts = contacts,
            onDeleteClick = { contact ->
                // 显示确认对话框
                AlertDialog.Builder(this)
                    .setTitle("删除联系人")
                    .setMessage("确定要删除 ${contact.name} 吗？")
                    .setPositiveButton("确定") { _, _ ->
                        if (contactsManager.deleteContact(contact.id)) {
                            adapter.removeContact(contact)
                            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            },
            onEditClick = { contact ->
                showEditContactDialog(contact)
            },
            onCallClick = { contact ->
                makePhoneCall(contact.phoneNumber)
            },
            onSmsClick = { contact ->
                showSmsDialog(contact)
            }
        )
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddContactDialog()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                ),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            loadContacts()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && 
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            loadContacts()
        } else {
            Toast.makeText(this, "需要联系人权限才能使用此功能", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val contactsList = contactsManager.getContacts()
            withContext(Dispatchers.Main) {
                contacts.clear()
                contacts.addAll(contactsList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    // 显示添加联系人对话框
    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.etPhone)

        AlertDialog.Builder(this)
            .setTitle("添加联系人")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val name = nameEditText.text.toString()
                val phone = phoneEditText.text.toString()
                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = contactsManager.addContact(name, phone)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(this@MainActivity, "添加成功", Toast.LENGTH_SHORT).show()
                                loadContacts() // 重新加载联系人列表
                            } else {
                                Toast.makeText(this@MainActivity, "添加失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 显示编辑联系人对话框
    private fun showEditContactDialog(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_contact, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.etName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.etPhone)

        nameEditText.setText(contact.name)
        phoneEditText.setText(contact.phoneNumber)

        AlertDialog.Builder(this)
            .setTitle("编辑联系人")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val newName = nameEditText.text.toString()
                val newPhone = phoneEditText.text.toString()
                if (newName.isNotEmpty() && newPhone.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = contactsManager.updateContact(contact.id, newName, newPhone)
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(this@MainActivity, "修改成功", Toast.LENGTH_SHORT).show()
                                loadContacts() // 重新加载联系人列表
                            } else {
                                Toast.makeText(this@MainActivity, "修改失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {
            // 请求拨打电话权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PERMISSION_REQUEST_CODE
            )
        } else {
            // 已有权限，直接拨打电话
            startCall(phoneNumber)
        }
    }

    private fun startCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法拨打电话", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showSmsDialog(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sms, null)
        val messageEditText = dialogView.findViewById<EditText>(R.id.etMessage)

        AlertDialog.Builder(this)
            .setTitle("发送短信给 ${contact.name}")
            .setView(dialogView)
            .setPositiveButton("发送") { _, _ ->
                val message = messageEditText.text.toString()
                if (message.isNotEmpty()) {
                    sendSms(contact.phoneNumber, message)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun sendSms(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "短信发送成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "短信发送失败", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
        private const val CALL_PERMISSION_REQUEST_CODE = 2
        private const val SMS_PERMISSION_REQUEST_CODE = 3
    }
}