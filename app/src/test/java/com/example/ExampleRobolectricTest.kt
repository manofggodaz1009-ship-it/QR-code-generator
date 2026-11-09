package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.upi.UpiManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CNV QR & UPI", appName)
  }

  @Test
  fun `test UPI URI parsing and construction`() {
    val uri = UpiManager.buildUpiUri(
      vpa = "merchant@oksbi",
      payeeName = "Store",
      amount = "250.00",
      note = "Billing"
    )
    val parsed = UpiManager.parseUpiUri(uri)
    assertNotNull(parsed)
    assertEquals("merchant@oksbi", parsed?.vpa)
    assertEquals("Store", parsed?.payeeName)
    assertEquals("250.00", parsed?.amount)
    assertEquals("Billing", parsed?.note)
  }
}
