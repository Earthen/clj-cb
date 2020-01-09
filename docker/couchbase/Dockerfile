FROM couchbase:6.0.3

COPY configure.sh /configure.sh
HEALTHCHECK CMD curl -u Administrator:Admin123 --fail http://localhost:8091/pools || exit 1
ENTRYPOINT ["/configure.sh"]
