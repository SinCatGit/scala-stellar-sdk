package stellar.sdk.op

import org.json4s.NoTypeHints
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.scalacheck.Arbitrary
import org.specs2.mutable.Specification
import stellar.sdk.DomainMatchers
import stellar.sdk.{ArbitraryInput, DomainMatchers}

class AccountMergeOperationSpec extends Specification with ArbitraryInput with DomainMatchers with JsonSnippets {

  implicit val arb: Arbitrary[Transacted[AccountMergeOperation]] = Arbitrary(genTransacted(genAccountMergeOperation))
  implicit val formats = Serialization.formats(NoTypeHints) + TransactedOperationDeserializer

  "account merge operation" should {
    "serde via xdr" >> prop { actual: AccountMergeOperation =>
      Operation.fromXDR(actual.toXDR) must beSuccessfulTry.like {
        case expected: AccountMergeOperation => expected must beEquivalentTo(actual)
      }
    }

    "parse from json" >> prop { op: Transacted[AccountMergeOperation] =>
      val doc =
        s"""
           | {
           |  "_links": {
           |    "self": {"href": "https://horizon-testnet.stellar.org/operations/10157597659144"},
           |    "transaction": {"href": "https://horizon-testnet.stellar.org/transactions/17a670bc424ff5ce3b386dbfaae9990b66a2a37b4fbe51547e8794962a3f9e6a"},
           |    "effects": {"href": "https://horizon-testnet.stellar.org/operations/10157597659144/effects"},
           |    "succeeds": {"href": "https://horizon-testnet.stellar.org/effects?order=desc\u0026cursor=10157597659144"},
           |    "precedes": {"href": "https://horizon-testnet.stellar.org/effects?order=asc\u0026cursor=10157597659144"}
           |  },
           |  "id": "${op.id}",
           |  "paging_token": "10157597659137",
           |  "source_account": "${op.sourceAccount.accountId}",
           |  "type_i": 8,
           |  "type": "account_merge"
           |  "created_at": "${formatter.format(op.createdAt)}",
           |  "transaction_hash": "${op.txnHash}",
           |  "account": "${op.sourceAccount.accountId}",
           |  "into": "${op.operation.destination.accountId}",
           |}
         """.stripMargin

      parse(doc).extract[Transacted[AccountMergeOperation]] mustEqual op

    }
  }

}
