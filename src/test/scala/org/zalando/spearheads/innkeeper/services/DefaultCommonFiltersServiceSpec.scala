package org.zalando.spearheads.innkeeper.services

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}
import org.zalando.spearheads.innkeeper.utils.InnkeeperEnvConfig

class DefaultCommonFiltersServiceSpec extends FunSpec with Matchers {

  describe("DefaultCommonFiltersService") {

    val envConfig = new InnkeeperEnvConfig(ConfigFactory.parseResources(this.getClass, "/common-filters.conf"))
    val commonFiltersService = new DefaultCommonFiltersService(envConfig)

    it("should return all common filters to prepend") {
      val commonFilters = commonFiltersService.getPrependFilters

      commonFilters should be(Seq("prepend(1)", "prepend(2)"))
    }

    it("should return all common filters to append") {
      val commonFilters = commonFiltersService.getAppendFilters

      commonFilters should be(Seq("append(1)", "append(2)"))
    }
  }

}
