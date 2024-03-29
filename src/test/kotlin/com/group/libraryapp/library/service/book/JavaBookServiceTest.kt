package com.group.libraryapp.library.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookCreateRequest
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import com.group.libraryapp.service.book.BookService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository
){

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
        bookRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록이 정상 작동한다.")
    fun saveBookTest() {
        // given
        val request = BookCreateRequest("이상한 나라의 엘리스", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        Assertions.assertThat(books).hasSize(1)
        Assertions.assertThat(books[0].name).isEqualTo("이상한 나라의 엘리스")
        Assertions.assertThat(books[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    @DisplayName("책 대출이 정상 작동한다.")
    fun loadBookTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("남영훈", 29))
        val request = BookLoanRequest("남영훈", "이상한 나라의 엘리스")

        // when
        bookService.loanBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        Assertions.assertThat(results).hasSize(1)
        Assertions.assertThat(results[0].bookName).isEqualTo("이상한 나라의 엘리스")
        Assertions.assertThat(results[0].user.id).isEqualTo(savedUser.id)
        Assertions.assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    @DisplayName("책이 대출되어 있을경우, 신규 대출 실패")
    fun loadBookFailTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("남영훈", 29))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "이상한 나라의 엘리스"))
        val request = BookLoanRequest("남영훈", "이상한 나라의 엘리스")

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        Assertions.assertThat(message).isEqualTo("대출중인 책입니다.")
    }

    @Test
    @DisplayName("책 반납이 정상 작동한다.")
    fun returnBookTest() {
        // given
        bookRepository.save(Book.fixture("이상한 나라의 엘리스"))
        val savedUser = userRepository.save(User("남영훈", 29))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser, "이상한 나라의 엘리스"))
        val request = BookReturnRequest("남영훈", "이상한 나라의 엘리스")

        // when
        bookService.returnBook(request)

        // then
        val results = userLoanHistoryRepository.findAll()
        Assertions.assertThat(results).hasSize(1)
        Assertions.assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    @DisplayName("책 대여 권수를 정상 확인한다.")
    fun countLoanedBookTest() {
        // given
        val savedUser = userRepository.save(User("남영훈", null))
        userLoanHistoryRepository.saveAll(listOf(
                UserLoanHistory.fixture(savedUser, "A"),
                UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
        ))

        // when
        val result = bookService.countLoanedBook()

        // then
        Assertions.assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
                Book.fixture("A", BookType.COMPUTER),
                Book.fixture("B", BookType.COMPUTER),
                Book.fixture("C", BookType.SCIENCE),
        ))

        // when
        val results = bookService.getBookStatistics()

        // then
        Assertions.assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.SCIENCE, 1L)
//        val computerDto = results.first { result -> result.type == BookType.COMPUTER }
//        Assertions.assertThat(computerDto.count).isEqualTo(2)
//
//        val scienceDto = results.first { result -> result.type == BookType.SCIENCE }
//        Assertions.assertThat(scienceDto.count).isEqualTo(1)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        Assertions.assertThat(results.first { result -> result.type == type }.count).isEqualTo(count)
    }

}