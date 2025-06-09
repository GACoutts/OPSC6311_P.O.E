package com.example.supa_budg.data

class EntryRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("categories")

    fun addCategory(category: Category) {
        dbRef.child(category.categoryid.toString()).setValue(category)
    }

    fun getAllCategories(callback: (List<Category>) -> Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<Category>()
                for (catSnapshot in snapshot.children) {
                    val cat = catSnapshot.getValue(Category::class.java)
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