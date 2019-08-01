package demo

import java.util.regex.Matcher
import java.util.regex.Pattern
import spock.lang.Specification

class KeycloakOauth2ClientSpec extends Specification {

    final String response = "{\"access_token\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0My1QamlGc3pUQnVtRmREenlsS1Y2TmhPalczY181VmhBaVFHdFZqMHhZIn0.eyJqdGkiOiI0MzVmYmI2My0wYTY5LTQxYTEtODRiNC1hMzdmMTc2Y2QxMzkiLCJleHAiOjE1NjQ1MDMwNDAsIm5iZiI6MCwiaWF0IjoxNTY0NTAyNDQwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaGNsYWJzLWRldiIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4OThlZWE4Ni0zMzgzLTQ2NzctOGE0OS02M2I1YjA2OThmN2MiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvanQiLCJhdXRoX3RpbWUiOjE1NjQ1MDIwNDUsInNlc3Npb25fc3RhdGUiOiJmMzRmMDQ5OS05YWNkLTQyOTctYTE1Zi05YTFmMmVlNjNmZDUiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJST0xFX09KVF9VU0VSIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiR3JlZyBKb2huc29uIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlciIsImdpdmVuX25hbWUiOiJHcmVnIiwiZmFtaWx5X25hbWUiOiJKb2huc29uIiwiZW1haWwiOiJ1c2VyQGVtYWlsLmNvbSJ9.RK9m6-me1NwNbdVvwW00n8gEOJJs4ZecrLAaVJSQV9LVQtEjZGZTa2dj8XahU0AJo6umAyn-4KWpqF-lboGQ7NuvETeuThBxjljOhoALmZFtX-sIivKQcR6VXGxfvkFgSY0jZg5AMEi_mn7lvjhFQTHLHURpsz4t7SsESk1O8tSGiNy0dGIQmtICAATC5szMiJWf284RSYlXL5bApg4_03QrOk4fsQPDAg4w_e8wpINacdmt45NXZmoKSv6LqRABubWlONxv14zApots1j49kkjbNub7BZ4fDyFbtJqWIiryz8MuA9KfUO23JxnxODg0QXdpk6lcErEWHnYtsiplAw\",\"expires_in\":600,\"refresh_expires_in\":3600,\"refresh_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkOWRhNjM2Mi0zYzdmLTRjZWItYjVjMC05MjlhMzZjMGU3MDQifQ.eyJqdGkiOiI1NWEyMTZjYS00NmFmLTQ5MGMtYjRlNS1hZjI0MDJiNWU4MmUiLCJleHAiOjE1NjQ1MDYwNDAsIm5iZiI6MCwiaWF0IjoxNTY0NTAyNDQwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaGNsYWJzLWRldiIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9hdXRoL3JlYWxtcy9oY2xhYnMtZGV2Iiwic3ViIjoiODk4ZWVhODYtMzM4My00Njc3LThhNDktNjNiNWIwNjk4ZjdjIiwidHlwIjoiUmVmcmVzaCIsImF6cCI6Im9qdCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6ImYzNGYwNDk5LTlhY2QtNDI5Ny1hMTVmLTlhMWYyZWU2M2ZkNSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsIlJPTEVfT0pUX1VTRVIiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUifQ.rxDIefyHkPOWWGBWEVUGyekr5h2Y_bTkAEfZrJqQmiU\",\"token_type\":\"bearer\",\"id_token\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0My1QamlGc3pUQnVtRmREenlsS1Y2TmhPalczY181VmhBaVFHdFZqMHhZIn0.eyJqdGkiOiI0NmU5ZjM0Yi0zMzc3LTQxNDAtOTQ0Yi00NjkzZjhiMDkyYmIiLCJleHAiOjE1NjQ1MDMwNDAsIm5iZiI6MCwiaWF0IjoxNTY0NTAyNDQwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaGNsYWJzLWRldiIsImF1ZCI6Im9qdCIsInN1YiI6Ijg5OGVlYTg2LTMzODMtNDY3Ny04YTQ5LTYzYjViMDY5OGY3YyIsInR5cCI6IklEIiwiYXpwIjoib2p0IiwiYXV0aF90aW1lIjoxNTY0NTAyMDQ1LCJzZXNzaW9uX3N0YXRlIjoiZjM0ZjA0OTktOWFjZC00Mjk3LWExNWYtOWExZjJlZTYzZmQ1IiwiYWNyIjoiMCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkdyZWcgSm9obnNvbiIsInByZWZlcnJlZF91c2VybmFtZSI6InVzZXIiLCJnaXZlbl9uYW1lIjoiR3JlZyIsImZhbWlseV9uYW1lIjoiSm9obnNvbiIsImVtYWlsIjoidXNlckBlbWFpbC5jb20ifQ.F9SEia4huTd_no5UGwhRzX82qRWJzePYNp8LF8jzx01o05qM5Qk7OiWFwBzau9V74BT4cv8cSAnAWHKBbVwlwhr46bAQFITkCh1YuFqSvZY4XDPhIbvfcKaYmVvC6Pm-W9xfJ2VSXo3lby3IN9LpsDkA-1G15SJXS9JWVpBErLHBQtazQvTOF-mrQgPBnHtr3JRTrV40leQfja2bmza-0guuldmk-ytZ_OuWngCHpaWRNJZULMaaeze4X1DFkQVt1eJuHdOVJfVP89Ouk-8pBN1ZYCtQUz2of2WkJiQCQgx7U8Gy3JxEVGY1GoOItfsm4z5C1xn7jUhHtEcVXfz6xA\",\"not-before-policy\":0,\"session_state\":\"f34f0499-9acd-4297-a15f-9a1f2ee63fd5\",\"scope\":\"openid email profile\"}"

    def setup() {
    }

    def cleanup() {
    }

    void 'test refresh token regex'() {
        given:
        final String REFRESH_TOKEN_REGEX = "\"refresh_token\"\\s*:\\s*\"(\\S*?)\""

        when:
        final Matcher matcher = Pattern.compile(REFRESH_TOKEN_REGEX).matcher(response)
        Boolean matchFound = matcher.find()

        then:
        matchFound == true

        when:
        String refresh_token = matcher.group(1)

        then:
        refresh_token != null
    }

    void 'test scope regex'() {
        given:
        final String SCOPE_REGEX = "\"scope\"\\s*:\\s*\"(\\S+.*?)\""

        when:
        final Matcher matcher = Pattern.compile(SCOPE_REGEX).matcher(response)
        Boolean matchFound = matcher.find()

        then:
        matchFound == true

        when:
        String scope = matcher.group(1)

        then:
        scope != null
    }

    void 'test fetching roles from access token'() {
        given:
        final String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0My1QamlGc3pUQnVtRmREenlsS1Y2TmhPalczY181VmhBaVFHdFZqMHhZIn0.eyJqdGkiOiJmYTI4NzBlOS0wZTY2LTQ1NGUtOTFmNy01MTgxNTMwMmJhOTAiLCJleHAiOjE1NjQ2NzQ5NjIsIm5iZiI6MCwiaWF0IjoxNTY0Njc0MzYyLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaGNsYWJzLWRldiIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4OThlZWE4Ni0zMzgzLTQ2NzctOGE0OS02M2I1YjA2OThmN2UiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJvanQiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJiMDNiZjFhMC1iMDFmLTQ3NjUtOTNmNi00YWMwYWIzOWMzMjciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJST0xFX09KVF9VU0VSIiwidW1hX2F1dGhvcml6YXRpb24iLCJST0xFX09KVF9BRE1JTiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkFkbWluIFVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbnVzZXIiLCJnaXZlbl9uYW1lIjoiQWRtaW4iLCJmYW1pbHlfbmFtZSI6IlVzZXIiLCJlbWFpbCI6ImFkbWludXNlckBlbWFpbC5jb20ifQ.EtVvZbxPM9Dk2ktmEthjUjthzpipSg_95zSAWAXTz5ZETZ-vLXvjb8KR_owHN8GPm3Tsjls__O5BjpFFgg2WWuqmxGQm4m4ZNuvb8FWvJdP5tPc-jAngXWAxh72zXFF4IEYPZttHTyt5ho4G2BZquvEOr55DCqrgweterzl04ZpQ3iu5V8SgTmiIA7U07xLl4EboJUDaVgroKqKQBW0M4O8kmHCNDGjbMFfafiqfOLHOlNesE82Xw81UqaHkG-A4T4ANTlkXunmetFcri64IpqlLZGEgcN2_nfbDSAa-8gLCF00bWTRuUpXmraHeUDQsiiTN_u_HjqfbL0Y8X_cYGA"

        when:
        def roles = Keycloak2Profile.testFetchingRoles(accessToken)

        then:
        roles != null
        roles.size() == 4
    }
}
