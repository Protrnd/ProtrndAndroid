package protrnd.com.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import protrnd.com.data.models.Transaction
import protrnd.com.data.network.api.PaymentApi
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class TransactionsPagingSource(private val api: PaymentApi) : PagingSource<Int, Transaction>() {

    override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val transactions = api.getTransactionsPaginated(position).data

            LoadResult.Page(
                data = transactions,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (transactions.isEmpty()) null else position + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }


}