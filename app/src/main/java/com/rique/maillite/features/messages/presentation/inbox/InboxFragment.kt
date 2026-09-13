package com.rique.maillite.features.messages.presentation.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.rique.maillite.R
import com.rique.maillite.core.extensions.applyWindowInsets
import com.rique.maillite.databinding.FragmentInboxBinding
import com.rique.maillite.features.messages.domain.model.Message
import com.rique.maillite.features.messages.presentation.MessageDetailResult
import com.rique.maillite.features.messages.presentation.inbox.adapter.MessagesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InboxViewModel by viewModels()

    private val adapter = MessagesAdapter(onMessageClick = ::navigateToDetail)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowInsets(
            rootView = binding.rootInbox,
            topView = null,
            bottomView = binding.fabComposeInbox
        )
        setupRecyclerView()
        setupSwipeToDelete()
        setupListeners()
        setupMenu()
        setupDetailResultListener()
        observeUiState()
        observeLoadMoreError()
        observeLoggedOut()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_inbox, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.action_logout) {
                        viewModel.logout()
                        return true
                    }
                    return false
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    private fun observeLoggedOut() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loggedOut.collect {
                    findNavController().navigate(R.id.action_inboxFragment_to_loginFragment)
                }
            }
        }
    }

    private fun setupDetailResultListener() {
        parentFragmentManager.setFragmentResultListener(
            MessageDetailResult.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val messageId = bundle.getLong(MessageDetailResult.KEY_MESSAGE_ID)
            when (bundle.getString(MessageDetailResult.KEY_ACTION)) {
                MessageDetailResult.ACTION_READ -> viewModel.markLocalAsRead(messageId)
                MessageDetailResult.ACTION_DELETED -> viewModel.removeLocal(messageId)
            }
        }
    }

    private fun observeLoadMoreError() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadMoreError.collect { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerMessagesInbox.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InboxFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return

                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    val totalItems = layoutManager.itemCount

                    if (lastVisible >= totalItems - SCROLL_THRESHOLD) {
                        viewModel.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val message = adapter.currentList[position]
                viewModel.deleteMessage(message)
                showUndoSnackbar(message)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerMessagesInbox)
    }

    private fun showUndoSnackbar(message: Message) {
        Snackbar.make(
            binding.root,
            getString(R.string.inbox_delete_undo_message, message.subject),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.inbox_delete_undo_action) {
                viewModel.undoDelete()
            }
            addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    // Só confirma a exclusão de verdade se NÃO foi o botão "Desfazer" que fechou
                    // a Snackbar (timeout, swipe, nova Snackbar substituindo etc. contam como
                    // "não desfez a tempo").
                    if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        viewModel.confirmPendingDelete()
                    }
                }
            })
        }.show()
    }

    private fun setupListeners() {
        binding.fabComposeInbox.setOnClickListener {
            findNavController().navigate(R.id.action_inboxFragment_to_composeMessageFragment)
        }

        binding.buttonRetryInbox.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun navigateToDetail(message: Message) {
        val action = InboxFragmentDirections.actionInboxFragmentToMessageDetailFragment(message.id)
        findNavController().navigate(action)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: InboxUiState) {
        binding.progressBarInbox.visibility = toVisibility(state is InboxUiState.Loading)
        binding.layoutEmptyStateInbox.visibility = toVisibility(state is InboxUiState.Empty)
        binding.layoutErrorStateInbox.visibility = toVisibility(state is InboxUiState.Error)
        binding.recyclerMessagesInbox.visibility = toVisibility(state is InboxUiState.Success)

        when (state) {
            is InboxUiState.Loading,
            is InboxUiState.Empty -> {
                binding.progressBarLoadMoreInbox.visibility = View.GONE
            }

            is InboxUiState.Error -> {
                binding.textErrorMessageInbox.text = state.message
                binding.progressBarLoadMoreInbox.visibility = View.GONE
            }

            is InboxUiState.Success -> {
                adapter.submitList(state.messages)
                binding.progressBarLoadMoreInbox.visibility = toVisibility(state.isLoadingMore)
            }
        }
    }

    private fun toVisibility(condition: Boolean): Int = if (condition) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerMessagesInbox.adapter = null
        _binding = null
    }

    private companion object {
        const val SCROLL_THRESHOLD = 5
    }
}