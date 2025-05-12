package com.example.soundnest_android.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.soundnest_android.R

class NotificationDetailDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_TITLE     = "arg_title"
        private const val ARG_BODY      = "arg_body"
        private const val ARG_SENDER    = "arg_sender"
        private const val ARG_RELEVANCE = "arg_relevance"
        private const val ARG_DATE      = "arg_date"

        fun newInstance(
            title: String?,
            body: String,
            sender: String,
            relevance: String?,
            date: String
        ) = NotificationDetailDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_BODY, body)
                putString(ARG_SENDER, sender)
                putString(ARG_RELEVANCE, relevance)
                putString(ARG_DATE, date)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sin título y con animación de diálogo
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title      = arguments?.getString(ARG_TITLE).takeIf { !it.isNullOrBlank() } ?: "New notification"
        val body       = requireArguments().getString(ARG_BODY)!!
        val sender     = requireArguments().getString(ARG_SENDER)!!
        val relevance  = requireArguments().getString(ARG_RELEVANCE)!!
        val date       = requireArguments().getString(ARG_DATE)!!

        view.findViewById<TextView>(R.id.tvDetailTitle).text     = title
        view.findViewById<TextView>(R.id.tvDetailBody).text      = body
        view.findViewById<TextView>(R.id.tvDetailSender).text    = "From: $sender"
        view.findViewById<TextView>(R.id.tvDetailRelevance).text = "Relevance: $relevance"
        view.findViewById<TextView>(R.id.tvDetailDate).text      = date
    }
}
