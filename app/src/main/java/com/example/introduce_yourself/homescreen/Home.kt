package com.example.introduce_yourself.homescreen

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.introduce_yourself.Activities.MainActivity
import com.example.introduce_yourself.Activities.UserItemActivity
import com.example.introduce_yourself.Models.ReadUserModel
import com.example.introduce_yourself.R
import com.example.introduce_yourself.database.User
import com.recyclerviewapp.UsersList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 *
 */
class Home : Fragment() {
    private var readUserModelList = ArrayList<ReadUserModel>()

    companion object{
        var USER_DETAILS = "user_details"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        getUsersList()
//        if (readUserModelList.size > 0){
//            usersRecyclerView(readUserModelList)
//        }
//        val viewAdapter = MyAdapter(Array(10) { "Person ${it + 1}" })
        val viewAdapter = UsersList(readUserModelList)

        view.findViewById<RecyclerView>(R.id.main_recycler_view).run {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter


//            viewAdapter.setOnClickListener(object : UsersList.OnClickListener{
//                override fun onClick(position: Int, model: ReadUserModel) {
//                    val intent = Intent(
//                        this,
//                        UserItemActivity::class.java
//                    )
//
//                    intent.putExtra(
//                        MainActivity.USER_DETAILS,
//                        model
//                    )
//
//                    startActivity(intent)
//                }
//            })
        }

        return view
    }

//    private fun usersRecyclerView(readUserModelList: ArrayList<ReadUserModel>){
//        val usersList = UsersList(this, readUserModelList)
//
//        usersList.setOnClickListener(object : UsersList.OnClickListener{
//            override fun onClick(position: Int, model: ReadUserModel) {
//                val intent = Intent(
//                    this@MainActivity,
//                    UserItemActivity::class.java
//                )
//
//                intent.putExtra(
//                    MainActivity.USER_DETAILS,
//                    model
//                )
//
//                startActivity(intent)
//            }
//        })
//    }

    private fun getUsersList() = runBlocking{
        newSuspendedTransaction(Dispatchers.IO) {
            val list = User.all().limit(5).toList()
            if (list.isNotEmpty())
                exposedToModel(list)
        }
    }
    private fun exposedToModel(list: List<User>){
        for(i in list)
            readUserModelList.add(
                ReadUserModel(
                    name = i.name,
                    surname = i.surname,
                    email = i.email,
                    description = "Opis Opis Opis Opis Opis Opis Opis Opis Opis Opis Opis Opis Opis ",
                    profile_picture = i.profile_picture.bytes
                )
            )
    }
}