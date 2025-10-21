package com.example.prm392_android_app_frontend.presentation.fragment.user;

    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ProgressBar;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

    import com.example.prm392_android_app_frontend.R;
    import com.example.prm392_android_app_frontend.core.util.Resource;
    import com.example.prm392_android_app_frontend.data.dto.BlogDto;
    import com.example.prm392_android_app_frontend.presentation.adapter.BlogAdapter;
    import com.example.prm392_android_app_frontend.presentation.viewmodel.BlogViewModel;

    import java.util.ArrayList;
    import java.util.List;

    public class BlogListFragment extends Fragment {

        private SwipeRefreshLayout swipe;
        private RecyclerView rv;
        private ProgressBar progress;

        private final List<BlogDto> data = new ArrayList<>();
        private BlogAdapter adapter;
        private BlogViewModel viewModel;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_blog_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(v, savedInstanceState);

            swipe = v.findViewById(R.id.swipe);
            rv = v.findViewById(R.id.rvBlogs);
            progress = v.findViewById(R.id.progress);

            rv.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new BlogAdapter(data, item -> {

                Fragment detail = BlogDetailFragment.newInstance(item);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                        )
                        .replace(R.id.fragment_container, detail)
                        .addToBackStack("blog_detail")
                        .commit();
            });



            rv.setAdapter(adapter);

            viewModel = new ViewModelProvider(this).get(BlogViewModel.class);
            observeViewModel();

            swipe.setOnRefreshListener(() -> viewModel.fetchBlogs());

            showLoading(true);
            viewModel.fetchBlogs();
        }

        private void observeViewModel() {
            viewModel.getBlogsState().observe(getViewLifecycleOwner(), res -> {
                if (res == null) return;

                switch (res.getStatus()) {
                    case LOADING:
                        showLoading(true);
                        break;
                    case SUCCESS:
                        showLoading(false);
                        swipe.setRefreshing(false);
                        List<BlogDto> items = res.getData();
                        data.clear();
                        if (items != null) data.addAll(items);
                        adapter.notifyDataSetChanged();
                        break;
                    case ERROR:
                        showLoading(false);
                        swipe.setRefreshing(false);
                        Toast.makeText(requireContext(),
                                res.getMessage() != null ? res.getMessage() : "Lỗi tải blog",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }

        private void showLoading(boolean isLoading) {
            if (progress != null)
                progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (rv != null) rv.setAdapter(null);
            swipe = null;
            rv = null;
            progress = null;
            adapter = null;
        }
    }
