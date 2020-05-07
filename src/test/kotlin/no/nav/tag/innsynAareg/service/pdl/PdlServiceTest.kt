package no.nav.tag.innsynAareg.service.sts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain

import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

import no.nav.tag.innsynAareg.service.pdl.PdlService

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@ExperimentalCoroutinesApi
@TestPropertySource(properties = ["mock.port=8082"])
class PdlServiceTest() {
    @Autowired
    lateinit var pdlService: PdlService

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Test
    fun testGetNavn() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val navn = pdlService.getFraPdl("4444");
        assertEquals("Ola", navn!!.fornavn)
    }
}

@ExperimentalCoroutinesApi
class CoroutineTestRule(val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}