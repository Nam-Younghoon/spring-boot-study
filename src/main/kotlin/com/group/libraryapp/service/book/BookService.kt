package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookCreateRequest
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import com.group.libraryapp.repository.book.BookQuerydslRepository
import com.group.libraryapp.repository.user.loanhistory.UserLoanHistoryQuerydslRepository
import com.group.libraryapp.util.fail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val bookQuerydslRepository: BookQuerydslRepository,
    private val userLoanHistoryQuerydslRepository: UserLoanHistoryQuerydslRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun saveBook(request: BookCreateRequest) {
        bookRepository.save(Book(request.name, request.type))
    }

    @Transactional
    fun loanBook(request: BookLoanRequest) {
        val book = bookRepository.findByName(request.bookName) ?: fail()

        if(userLoanHistoryQuerydslRepository.find(book.name, UserLoanStatus.LOANED) != null) {
            throw IllegalArgumentException("대출중인 책입니다.")
        }

        val user = userRepository.findByName(request.userName) ?: fail()
        user.loanBook(book.name)
    }

    @Transactional
    fun returnBook(request: BookReturnRequest) {
        val user = userRepository.findByName(request.userName) ?: fail()
        user.returnBook(request.bookName)
    }

    @Transactional(readOnly = true)
    fun countLoanedBook(): Int {
//        return userLoanHistoryRepository.findAllByStatus(UserLoanStatus.LOANED).size
        return userLoanHistoryQuerydslRepository.count(UserLoanStatus.LOANED).toInt()
    }

    @Transactional(readOnly = true)
    fun getBookStatistics(): List<BookStatResponse> {
        return bookQuerydslRepository.getStatus()

//        return bookRepository.getStatus()

//        return bookRepository.findAll() // List<Book>
//                .groupBy { book -> book.type } // Map<BookType, List<Book>>
//                .map { (type, books) -> BookStatResponse(type, books.size) } // List<BookStatResponse>

//        val results = mutableListOf<BookStatResponse>()
//        val books = bookRepository.findAll()
//        for (book in books) {
//            val targetDto = results.firstOrNull { dto -> book.type == dto.type }?.plusOne()
//                    ?: results.add(BookStatResponse(book.type, 1))
//        }
//        return results
    }


}