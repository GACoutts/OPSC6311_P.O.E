package com.example.supa_budg.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("categories")

    fun addCategory(category: Category) {
        dbRef.child(category.categoryid.toString()).setValue(category)
    }

    fun getAllCategories(callback: (List<Category>) -> Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Category>()
                for (child in snapshot.children) {
                    val cat = child.getValue(Category::class.java)
                    if (cat != null) result.add(cat)
                }
                callback(result)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read: ${error.message}")
                callback(emptyList())
            }
        })
    }
}