import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class BisecureAuthTest {

    @Test
    fun testAuth() {
        val auth2 = "0000000000005410EC03615000190000000000100674686F6D61736161616262626363632DF0"
        val createdAuth = BisecureAuth().createAuth("000000000000", "5410EC036150", "thomas", "aaabbbccc")
        println(auth2)
        println(createdAuth)
        assertTrue { auth2.equals(createdAuth) }
    }

}
