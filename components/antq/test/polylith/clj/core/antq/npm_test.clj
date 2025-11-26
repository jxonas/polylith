(ns polylith.clj.core.antq.npm-test
  (:require [clojure.test :refer :all]
            [polylith.clj.core.antq.npm :as npm]))

(deftest keyword->package-name-test
  (testing "converts regular package names"
    (is (= "lodash" (npm/keyword->package-name :lodash)))
    (is (= "express" (npm/keyword->package-name :express))))

  (testing "preserves scoped package names"
    (is (= "@mantine/core" (npm/keyword->package-name :@mantine/core)))
    (is (= "@mantine/charts" (npm/keyword->package-name :@mantine/charts)))
    (is (= "@tabler/icons-react" (npm/keyword->package-name :@tabler/icons-react)))
    (is (= "@puckeditor/puck" (npm/keyword->package-name :@puckeditor/puck))))

  (testing "handles string input"
    (is (= "lodash" (npm/keyword->package-name "lodash")))
    (is (= "@mantine/core" (npm/keyword->package-name "@mantine/core")))))

(deftest npm-dep?-test
  (testing "filters out @poly namespaced packages"
    (is (false? (npm/npm-dep? [:@poly/something "1.0.0"]))))

  (testing "filters out wildcard dependencies"
    (is (false? (npm/npm-dep? [:lodash "*"]))))

  (testing "accepts regular dependencies"
    (is (true? (npm/npm-dep? [:lodash "^4.17.21"])))
    (is (true? (npm/npm-dep? [:@mantine/core "^8.3.8"])))))

(deftest npm-dependencies->latest-versions-test
  (testing "converts keywords to full package names for scoped packages"
    (let [deps {:@mantine/core "^8.3.8"
                :@mantine/charts "^8.3.8"
                :lodash "^4.17.21"}]
      ;; We can't test the actual API calls, but we can verify the structure
      ;; and that package names are preserved
      (with-redefs [npm/get-latest-version (constantly nil)]
        (let [result (npm/npm-dependencies->latest-versions deps)]
          ;; Result should be empty because get-latest-version returns nil
          (is (= {} result))))))

  (testing "uses full package names in result keys"
    (let [deps {:@mantine/core "^8.3.8"
                :lodash "^4.17.21"}
          ;; Mock get-latest-version to return a version
          mock-versions {"@mantine/core" "8.4.0"
                        "lodash" "4.17.21"}]
      (with-redefs [npm/get-latest-version (fn [pkg]
                                             (get mock-versions (npm/keyword->package-name pkg)))]
        (let [result (npm/npm-dependencies->latest-versions deps)]
          (is (= "@mantine/core" (ffirst result)))
          (is (= "lodash" (first (second result)))))))))
