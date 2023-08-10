package com.example.shoppingscanner.presentation.ui.productlist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppingscanner.R
import com.example.shoppingscanner.domain.dto.ListProduct
import com.example.shoppingscanner.domain.usecase.GetProductList
import com.example.shoppingscanner.presentation.ui.base.BaseEvent
import com.example.shoppingscanner.presentation.ui.shared.SharedViewModel
import com.example.shoppingscanner.presentation.ui.shared.ShoppingListState
import com.example.shoppingscanner.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductListUseCase: GetProductList,
    private val sharedViewModel: SharedViewModel,
    ) : ViewModel() {

    private val _state = mutableStateOf(ProductListState())
    val state : State<ProductListState> = _state

    var productList: List<ListProduct>? = listOf()

    var shoppingListState: State<ShoppingListState> = sharedViewModel.shoppingListState


    private fun setCategoryName(category: String): String {
        return when (category) {
            "Ayakkabı" -> "shoe"
            "Çanta" -> "luggage"
            "Dekorasyon" -> "home"
            "Koltuk" -> "chair"
            else -> "shoe"
        }
    }

    fun getProductListFromAPI(category:String) {
        val apiCategory = setCategoryName(category)
        getProductListUseCase.executeGetProductList(apiCategory).onEach {
            when(it){
                is Resource.Success -> {
                    productList = it.data
                    _state.value = _state.value.copy(
                        productList = productList)
                }
                is Resource.Error -> {
                    println(it.message)
                    _state.value = state.value.copy(error = it.message, messageId = R.string.try_again)
                }
                is Resource.Loading -> {
                    _state.value = state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun addToList(currentProduct:ListProduct) {
        var shoppingList = shoppingListState.value.shoppingList

        val existingProduct = shoppingList.find {
            it.barcode_number == currentProduct?.barcode_number
        }
        if (existingProduct != null) {
            _state.value.copy(messageId = R.string.product_exist)
        } else {
            _state.value = _state.value.copy(
                shoppingList = _state.value.shoppingList?.plus(currentProduct),
            )
            shoppingListState.value.shoppingList = shoppingList.plus(currentProduct)

            println(shoppingListState.value!!.shoppingList.size)
        }
    }

    fun onEvent(event: BaseEvent){
        when (event){
            is BaseEvent.GetData -> {
                getProductListFromAPI(event.category)
            }
            is BaseEvent.OnHandledMessage -> {
                _state.value.copy(messageId = null)
            }else -> {

            }
        }
    }

}