package com.example.prm392_android_app_frontend.presentation.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_android_app_frontend.R;
import com.example.prm392_android_app_frontend.data.dto.ApiError;
import com.example.prm392_android_app_frontend.data.dto.ArtistDto;
import com.example.prm392_android_app_frontend.data.dto.CategoryDto;
import com.example.prm392_android_app_frontend.data.dto.ProductFilter;
import com.example.prm392_android_app_frontend.data.dto.PublisherDto;
import com.example.prm392_android_app_frontend.data.remote.ErrorUtils;
import com.example.prm392_android_app_frontend.data.remote.api.ApiClient;
import com.example.prm392_android_app_frontend.data.remote.api.ArtistApi;
import com.example.prm392_android_app_frontend.data.remote.api.CategoryApi;
import com.example.prm392_android_app_frontend.data.remote.api.PublisherApi;
import com.example.prm392_android_app_frontend.data.repository.ArtistRepository;
import com.example.prm392_android_app_frontend.data.repository.CategoryRepository;
import com.example.prm392_android_app_frontend.data.repository.PublisherRepository;
import com.example.prm392_android_app_frontend.presentation.adapter.ProductAdapter;
import com.example.prm392_android_app_frontend.presentation.viewmodel.CartViewModel;
import com.example.prm392_android_app_frontend.presentation.viewmodel.ProductViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.widget.Toast;

public class SearchProductActivity extends AppCompatActivity {

    // UI Components
    private EditText edtQuery;
    private RecyclerView rvProducts;
    private ProgressBar progress;
    private LinearLayout btnSort, btnFilter;

    private ProductAdapter adapter;
    private ProductViewModel viewModel;
    private CartViewModel cartViewModel;

    private String currentQuery = "";
    private ProductFilter currentFilter = null;

    private CategoryRepository categoryRepo;
    private ArtistRepository artistRepo;
    private PublisherRepository publisherRepo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        // Repo cho category
        CategoryApi categoryApi = com.example.prm392_android_app_frontend.data.remote.api.ApiClient
                .get().create(CategoryApi.class);
        categoryRepo = new CategoryRepository(categoryApi);
        
        // Repo cho artist
        ArtistApi artistApi = com.example.prm392_android_app_frontend.data.remote.api.ApiClient.get().create(ArtistApi.class);
        artistRepo = new ArtistRepository(artistApi);
        
        // Repo cho publisher
        PublisherApi publisherApi = com.example.prm392_android_app_frontend.data.remote.api.ApiClient.get().create(PublisherApi.class);
        publisherRepo = new PublisherRepository(publisherApi);
        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        edtQuery   = findViewById(R.id.editQuery);
        rvProducts = findViewById(R.id.rvProduct);
        progress   = findViewById(R.id.progress);
        btnSort    = findViewById(R.id.btnSort);
        btnFilter  = findViewById(R.id.btnFilter);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter();
        rvProducts.setAdapter(adapter);


        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(ProductViewModel.class);
        
        cartViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(CartViewModel.class);

        // Setup adapter listener để xử lý thêm vào giỏ hàng
        adapter.setOnAddToCartClickListener((productId, quantity) -> {
            cartViewModel.addProductToCart(productId, quantity);
        });

        observeViewModel();


        edtQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });

        // Sort / Filter dialog
        btnSort.setOnClickListener(v -> showSortDialog());
        btnFilter.setOnClickListener(v -> showFilterDialog());

        edtQuery.requestFocus();
    }

    private void showSortDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View content = LayoutInflater.from(this).inflate(R.layout.diaglog_sort, null, false);
        dialog.setContentView(content);

        View optionPriceHighToLow = content.findViewById(R.id.highToLow);
        View optionPriceLowToHigh = content.findViewById(R.id.lowToHigh);

        optionPriceHighToLow.setOnClickListener(v -> {
            if (currentFilter == null) currentFilter = new ProductFilter();
            currentFilter.priceSort = "high_to_low";
            Toast.makeText(this, "Đã chọn: Giá cao xuống thấp", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            if (!currentQuery.isEmpty()) viewModel.search(currentQuery, currentFilter);
        });

        optionPriceLowToHigh.setOnClickListener(v -> {
            if (currentFilter == null) currentFilter = new ProductFilter();
            currentFilter.priceSort = "low_to_high";
            Toast.makeText(this, "Đã chọn: Giá thấp lên cao", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            if (!currentQuery.isEmpty()) viewModel.search(currentQuery, currentFilter);
        });

        dialog.show();
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.diaglog_filter, null, false);
        dialog.setContentView(view);

        ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        ChipGroup chipGroupArtist = view.findViewById(R.id.chipGroupArtist);
        ChipGroup chipGroupPublisher = view.findViewById(R.id.chipGroupPublisher);
        View btnReset = view.findViewById(R.id.btnReset);
        View btnApply = view.findViewById(R.id.btnApply);

        // Nạp dữ liệu category, artist và publisher (chip)
        loadCategoriesIntoChips(chipGroupCategory);
        loadArtistsIntoChips(chipGroupArtist);
        loadPublishersIntoChips(chipGroupPublisher);



        btnReset.setOnClickListener(v -> {
            chipGroupCategory.clearCheck();
            chipGroupArtist.clearCheck();
            chipGroupPublisher.clearCheck();
            if (currentFilter != null) {
                currentFilter.categoryId = null;
                currentFilter.artistId = null;
                currentFilter.publisherId = null;
            }
            Toast.makeText(this, "Đã thiết lập lại bộ lọc", Toast.LENGTH_SHORT).show();
        });


        btnApply.setOnClickListener(v -> {
            if (currentFilter == null) currentFilter = new ProductFilter();
            currentFilter.categoryId = getCheckedChipTagAsInt(chipGroupCategory);
            currentFilter.artistId = getCheckedChipTagAsInt(chipGroupArtist);
            currentFilter.publisherId = getCheckedChipTagAsInt(chipGroupPublisher);
            android.util.Log.d("SearchActivity", "🔍 Applied filter: " + currentFilter.toString());
            android.util.Log.d("SearchActivity", "🔍 Current query: " + currentQuery);

            dialog.dismiss();
            if (!currentQuery.isEmpty()) {
                viewModel.search(currentQuery, currentFilter);
            } else {
                Toast.makeText(this, "Nhập từ khoá trước khi áp dụng bộ lọc", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void doSearch() {
        String q = edtQuery.getText() != null ? edtQuery.getText().toString().trim() : "";
        if (q.isEmpty()) return;
        currentQuery = q;
        viewModel.search(currentQuery, currentFilter);
    }

    private void observeViewModel() {
        viewModel.getProductsState().observe(this, res -> {
            if (res == null) return;
            switch (res.getStatus()) {
                case LOADING:
                    progress.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progress.setVisibility(View.GONE);
                    adapter.setProducts(res.getData());
                    break;
                case ERROR:
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this,
                            res.getMessage() != null ? res.getMessage() : "Lỗi tải dữ liệu",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        
        // Lắng nghe kết quả thêm vào giỏ hàng
        cartViewModel.getCartLiveData().observe(this, cartDto -> {
            if (cartDto != null) {
                Toast.makeText(this, 
                    "Đã thêm sản phẩm vào giỏ hàng thành công!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe lỗi từ CartViewModel
        cartViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, 
                    "Lỗi: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategoriesIntoChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        categoryRepo.getAll(new retrofit2.Callback<java.util.List<CategoryDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<CategoryDto>> call,
                                   retrofit2.Response<java.util.List<CategoryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (CategoryDto c : response.body()) {
                        Chip chip = (Chip) getLayoutInflater()
                                .inflate(R.layout.item_chip, chipGroup, false);
                        chip.setText(c.name);
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        chip.setTag(c.id);
                        // Set unique ID for the chip so ChipGroup can track selection
                        chip.setId(View.generateViewId());
                        chipGroup.addView(chip);
                        android.util.Log.d("SearchActivity", "🔍 Created chip: " + c.name + " with ID: " + chip.getId() + " and tag: " + c.id);
                        
                        // Test click listener để debug
                        chip.setOnClickListener(v -> {
                            android.util.Log.d("SearchActivity", "🔍 Category chip clicked: " + c.name + " ID: " + chip.getId());
                            android.util.Log.d("SearchActivity", "🔍 Chip checked: " + chip.isChecked());
                        });
                    }
                    // Chọn lại nếu đã có filter trước đó
                    if (currentFilter != null && currentFilter.categoryId != null) {
                        for (int i = 0; i < chipGroup.getChildCount(); i++) {
                            View v = chipGroup.getChildAt(i);
                            if (v instanceof Chip) {
                                Object tag = v.getTag();
                                if (tag instanceof Integer && ((Integer) tag).equals(currentFilter.categoryId)) {
                                    ((Chip) v).setChecked(true);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // API response không thành công
                    String errorMsg = parseApiError(response);
                    showError("Không tải được danh mục: " + errorMsg);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<CategoryDto>> call, Throwable t) {
                // Network error hoặc parsing error
                String errorMsg = (t.getMessage() != null) ? t.getMessage() : "Lỗi kết nối mạng";
                showError("Lỗi tải danh mục: " + errorMsg);
            }
        });
    }
    private void loadArtistsIntoChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        artistRepo.getAll(new retrofit2.Callback<java.util.List<ArtistDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<ArtistDto>> call,
                                   retrofit2.Response<java.util.List<ArtistDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ArtistDto a : response.body()) {
                        Chip chip = (Chip) getLayoutInflater()
                                .inflate(R.layout.item_chip, chipGroup, false);
                        chip.setText(a.artistName);
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        chip.setTag(a.id);
                        // Set unique ID for the chip so ChipGroup can track selection
                        chip.setId(View.generateViewId());
                        chipGroup.addView(chip);
                        android.util.Log.d("SearchActivity", "🎤 Created artist chip: " + a.artistName + " with ID: " + chip.getId() + " and tag: " + a.id);
                        
                        // Test click listener để debug
                        chip.setOnClickListener(v -> {
                            android.util.Log.d("SearchActivity", "🎤 Artist chip clicked: " + a.artistName + " ID: " + chip.getId());
                            android.util.Log.d("SearchActivity", "🎤 Chip checked: " + chip.isChecked());
                        });
                    }
                    
                    // Chọn lại nếu đã có filter trước đó
                    if (currentFilter != null && currentFilter.artistId != null) {
                        for (int i = 0; i < chipGroup.getChildCount(); i++) {
                            View v = chipGroup.getChildAt(i);
                            if (v instanceof Chip) {
                                Object tag = v.getTag();
                                if (tag instanceof Integer && ((Integer) tag).equals(currentFilter.artistId)) {
                                    ((Chip) v).setChecked(true);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // API response không thành công
                    String errorMsg = parseApiError(response);
                    showError("Không tải được danh sách ca sĩ: " + errorMsg);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<ArtistDto>> call, Throwable t) {
                // Network error hoặc parsing error
                String errorMsg = (t.getMessage() != null) ? t.getMessage() : "Lỗi kết nối mạng";
                showError("Lỗi tải ca sĩ: " + errorMsg);
            }
        });
    }

    private void loadPublishersIntoChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        publisherRepo.getAll(new retrofit2.Callback<java.util.List<PublisherDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<PublisherDto>> call,
                                   retrofit2.Response<java.util.List<PublisherDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (PublisherDto p : response.body()) {
                        Chip chip = (Chip) getLayoutInflater()
                                .inflate(R.layout.item_chip, chipGroup, false);
                        chip.setText(p.getName());
                        chip.setCheckable(true);
                        chip.setClickable(true);
                        chip.setTag(p.getId());
                        // Set unique ID for the chip so ChipGroup can track selection
                        chip.setId(View.generateViewId());
                        chipGroup.addView(chip);
                        android.util.Log.d("SearchActivity", "📚 Created publisher chip: " + p.getName() + " with ID: " + chip.getId() + " and tag: " + p.getId());
                        
                        // Test click listener để debug
                        chip.setOnClickListener(v -> {
                            android.util.Log.d("SearchActivity", "📚 Publisher chip clicked: " + p.getName() + " ID: " + chip.getId());
                            android.util.Log.d("SearchActivity", "📚 Chip checked: " + chip.isChecked());
                        });
                    }
                    
                    // Chọn lại nếu đã có filter trước đó
                    if (currentFilter != null && currentFilter.publisherId != null) {
                        for (int i = 0; i < chipGroup.getChildCount(); i++) {
                            View v = chipGroup.getChildAt(i);
                            if (v instanceof Chip) {
                                Object tag = v.getTag();
                                if (tag instanceof Integer && ((Integer) tag).equals(currentFilter.publisherId)) {
                                    ((Chip) v).setChecked(true);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // API response không thành công
                    String errorMsg = parseApiError(response);
                    showError("Không tải được danh sách nhà xuất bản: " + errorMsg);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<PublisherDto>> call, Throwable t) {
                // Network error hoặc parsing error
                String errorMsg = (t.getMessage() != null) ? t.getMessage() : "Lỗi kết nối mạng";
                showError("Lỗi tải nhà xuất bản: " + errorMsg);
            }
        });
    }

    /**
     * Helper method để parse API error response
     */
    private String parseApiError(retrofit2.Response<?> response) {
        try {
            ApiError apiError = ErrorUtils.parseError(ApiClient.get(), response);
            if (apiError != null && apiError.getMessage() != null && !apiError.getMessage().isEmpty()) {
                return apiError.getMessage();
            }
        } catch (Exception e) {
            android.util.Log.e("SearchActivity", "Error parsing API error: " + e.getMessage());
        }
        
        // Fallback error message
        return "Lỗi API (HTTP " + response.code() + ")";
    }

    /**
     * Helper method để hiển thị error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        android.util.Log.e("SearchActivity", "API Error: " + message);
    }


    @Nullable
    private Integer getCheckedChipTagAsInt(ChipGroup group) {
        int checkedId = group.getCheckedChipId();
        android.util.Log.d("SearchActivity", "🔍 Checked chip ID: " + checkedId);
        android.util.Log.d("SearchActivity", "🔍 Total chips in group: " + group.getChildCount());
        
        if (checkedId == View.NO_ID) {
            android.util.Log.d("SearchActivity", "🔍 No chip selected (NO_ID)");
            return null;
        }
        View chip = group.findViewById(checkedId);
        Object tag = chip != null ? chip.getTag() : null;
        android.util.Log.d("SearchActivity", "🔍 Chip tag: " + tag);
        if (tag instanceof Integer) return (Integer) tag;
        try { return tag != null ? Integer.parseInt(String.valueOf(tag)) : null; }
        catch (Exception e) { return null; }
    }
}
