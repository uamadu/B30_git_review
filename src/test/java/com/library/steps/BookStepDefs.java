package com.library.steps;

import com.library.pages.BookPage;
import com.library.utility.BrowserUtil;
import com.library.utility.DB_Util;
import com.library.utility.Driver;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

public class BookStepDefs  {

    BookPage bookPage=new BookPage();

    @When("the user navigates to {string} page")
    public void the_user_navigates_to_page(String moduleName) {
        bookPage.navigateModule(moduleName);
        BrowserUtil.waitFor(2);


    }
    List<String> actualCategoryList;
    @When("the user clicks book categories")
    public void the_user_clicks_book_categories() {
        actualCategoryList = BrowserUtil.getAllSelectOptions(bookPage.mainCategoryElement);
        System.out.println("actualCategoryList = " + actualCategoryList);

        // Remove ALL from list
        actualCategoryList.remove(0);
        System.out.println("After remove ALL Option");
        System.out.println("actualCategoryList = " + actualCategoryList);

    }

    @Then("verify book categories must match book_categories table from db")
    public void verify_book_categories_must_match_book_categories_table_from_db() {

        DB_Util.runQuery("select name from book_categories");

        List<String> expectedCategoryList = DB_Util.getColumnDataAsList(1);
        System.out.println("expectedCategoryList = " + expectedCategoryList);

        Assert.assertEquals(expectedCategoryList,actualCategoryList);


    }

    String searchedBook;
    @When("the user searches for {string} book")
    public void the_user_searches_for_book(String bookName) {
        bookPage.search.sendKeys(bookName);
        searchedBook = bookName;
    }
    @When("the user clicks edit book button")
    public void the_user_clicks_edit_book_button() {
        bookPage.editBook(searchedBook).click();
    }
    @Then("book information must match the Database")
    public void book_information_must_match_the_database() {
        String query = "select * from books where name = 'Clean Code Nataliia';";
        DB_Util.runQuery(query);
        Map<String, String> DB_Book_Info = DB_Util.getRowMap(1);

        // is to see if I have any sync issues
        System.out.println("bookPage.bookName.getText() = " + bookPage.bookName.getAttribute("value"));

        String bookNameFromUI = bookPage.bookName.getAttribute("value");
        String bookNameFromDB = DB_Book_Info.get("name");
        String isbnFromDB = DB_Book_Info.get("isbn");
        String isbnFromUI = bookPage.isbn.getAttribute("value");
        Assert.assertEquals(bookNameFromDB,bookNameFromUI);
        Assert.assertEquals(isbnFromDB,isbnFromUI);


    }

    String actualMostPopularGenre;
    @When("I execute query to find most popular book genre")
    public void iExecuteQueryToFindMostPopularBookGenre() {
        String query = "select name from book_categories " +
                "where id = (select book_category_id from books " +
                "where id = (select book_id from book_borrow group by book_id order by count(*) desc limit 1));";
        DB_Util.runQuery(query);
        actualMostPopularGenre = DB_Util.getFirstRowFirstColumn();
    }

    @Then("verify {string} is the most popular book genre.")
    public void verifyIsTheMostPopularBookGenre(String expectedGenre) {
        Assert.assertEquals(expectedGenre,actualMostPopularGenre);
    }

    @When("the user clicks Borrow Book")
    public void theUserClicksBorrowBook() {
        bookPage.borrowBook(searchedBook).click();

    }

    int locatorIndex;
    @Then("verify that book is shown in {string} page")
    public void verifyThatBookIsShownInPage(String module) {
        bookPage.navigateModule(module);
        List<String> borrowedOrNotList = BrowserUtil.getElementsText(bookPage.returnInfoColumnValues(searchedBook));
        System.out.println("borrowedOrNotList = " + borrowedOrNotList);
        locatorIndex = borrowedOrNotList.size();
    }

    @And("verify logged student has same book in database")
    public void verifyLoggedStudentHasSameBookInDatabase() {
        System.out.println("not implemented yet");
        String query = "select email from users where id = (select user_id from book_borrow where book_id = 22060 order by id desc limit 1);";
        DB_Util.runQuery(query);
        String actualUserEmail = DB_Util.getFirstRowFirstColumn();
        String expectedUserEmail = "student6@library";

        Assert.assertEquals(actualUserEmail,expectedUserEmail);

        WebElement returnBookButton = Driver.getDriver().findElement(By.xpath("(//td[.='"+ searchedBook +"']/../td/a)["+locatorIndex+"]")) ;
        returnBookButton.click();

    }
}