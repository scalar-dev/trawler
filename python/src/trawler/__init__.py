from inspect import getframeinfo, stack

from trawler.graph import Graph



# class GrpcAuth(grpc.AuthMetadataPlugin):
#     def __init__(self, client_id, client_key):
#         self._client_id = client_id
#         self._client_key = client_key

#     def __call__(self, context, callback):
#         callback((('client-id', self._client_id), ('client-key', self._client_key)), None)

# def configure(client_id, client_key):
#     global auth
#     auth = GrpcAuth(client_id, client_key)

# def record(df, locator):
#     ssl_creds = grpc.ssl_channel_credentials(root_certificates=open("../yak-collector/src/main/resources/certificate.crt", "rb").read())
#     call_credentials = grpc.metadata_call_credentials(auth)
#     channel_creds = grpc.composite_channel_credentials(ssl_creds, call_credentials)
#     channel = grpc.secure_channel('localhost:8080', channel_creds)
#     stub = trawler_grpc.TrawlerStub(channel)

#     request = trawler_proto.RecordRequest()
#     request.locator = locator
#     request.timestamp.GetCurrentTime()

#     caller = getframeinfo(stack()[1][0])
#     request.schema.comment = "%s:%d" % (caller.filename, caller.lineno)

#     for column in df.columns:
#         field = trawler_proto.Field()
#         field.name = column
#         field.type = str(df[column].dtype)
#         request.schema.field.append(field)

#         null_count = trawler_proto.Fact()
#         null_count.name = "yak.core.null-count"
#         null_count.field = field.name
#         null_count.double_value = df[column].isna().sum()
#         request.facts.facts.append(null_count)

#     row_count = trawler_proto.Fact()
#     row_count.name = "yak.core.row-count"
#     row_count.double_value = len(df)
#     request.facts.facts.append(row_count)

#     stub.Record(request)