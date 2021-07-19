package com.example.criminalintent.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.Crime
import com.example.criminalintent.R
import kotlinx.android.synthetic.main.fragment_crime.*
import kotlinx.android.synthetic.main.fragment_crime.crime_title
import kotlinx.android.synthetic.main.fragment_crime_list.*
import kotlinx.android.synthetic.main.list_item_crime.*
import kotlinx.android.synthetic.main.list_item_crime.view.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

@RequiresApi(Build.VERSION_CODES.O)

class CrimeListFragment : Fragment(), ListAction {


    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
        fun onAddButtonClick(crime: Crime)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null
    private val crimeListViewModel: CrimeListViewModel by viewModel()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?

    }

    companion object {
        fun newInstance(): CrimeListFragment = CrimeListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_crime_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CrimeAdapter(this)
        crimeRecyclerView.adapter = adapter

        add_crime.setOnClickListener {

            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onAddButtonClick(crime)
        }

        crimeListViewModel.crimeListLiveData
            .observe(
                viewLifecycleOwner
            ) { crimes ->
                crimes.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")

                    updateUI(crimes)
                }
            }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }


    private fun updateUI(crimes: List<Crime>?) {
        if (crimes != null) {
            no_data.isVisible = crimes.isEmpty()
            add_crime.isVisible = crimes.isEmpty()

        }
        adapter!!.submitList(crimes)
    }

    override fun removeCrime(id: UUID) {
        crimeListViewModel.removeCrime(id)
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var crime: Crime

        private var titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }


        fun bind(crime: Crime) {

            this.crime = crime
            titleTextView.text = crime.title
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale("Ukr"))
            try {
                val date = crime.date
                val dateTime = dateFormat.format(date)
                dateTextView.text = dateTime
            } catch (e: Exception) {
                e.printStackTrace()
            }

            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }

        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)

        }


    }


    private inner class CrimeAdapter(val listAction: ListAction) :
        ListAdapter<Crime, CrimeHolder>(DiffCallBack()) {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            Log.d(TAG, "onCreateViewHolder: ")
            return CrimeHolder(layoutInflater.inflate(R.layout.list_item_crime, parent, false))
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            Log.d(TAG, "bind, position = $position");
            val crime = getItem(position)

            holder.bind(crime)
            holder.itemView.delete_button.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes") { _: DialogInterface, _ ->
                        listAction.removeCrime(
                            crime.id
                        )
                    }
                    .setNegativeButton("Cancel") { _: DialogInterface, _ -> }
                    .create()
                    .show()

            }
        }

    }

    inner class DiffCallBack : DiffUtil.ItemCallback<Crime>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return newItem == oldItem
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")

    }


}